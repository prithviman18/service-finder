/**
 * Customer / User Portal Logic
 * 3-Step Guided Local Service Finder
 */

let selectedService = '';
let selectedLocation = '';
let availableServices = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!Auth.requireRole('USER')) return;
    Auth.initNavbar('USER');

    await loadServices();
    // Default initial search
    performSearch();
});

async function loadServices() {
    try {
        const res = await fetch('/api/services');
        if (res.ok) {
            availableServices = await res.json();
            renderServiceSelector(availableServices);
        }
    } catch (e) {
        console.warn('Failed to fetch services, using default set', e);
    }
}

function renderServiceSelector(services) {
    const grid = document.getElementById('service-selector-grid');
    if (!grid) return;

    grid.innerHTML = `
        <button type="button" class="service-card-btn selected" id="svc-all" onclick="selectService('')">
            <i class="fa-solid fa-border-all"></i>
            <span>All Services</span>
        </button>
    ` + services.map(s => `
        <button type="button" class="service-card-btn" id="svc-${s.code}" onclick="selectService('${s.code}')">
            <i class="fa-solid ${s.iconClass || 'fa-wrench'}"></i>
            <span>${escapeHtml(s.displayName)}</span>
        </button>
    `).join('');
}

function selectService(serviceCode) {
    selectedService = serviceCode;
    
    // Update active UI card
    document.querySelectorAll('.service-card-btn').forEach(btn => btn.classList.remove('selected'));
    if (!serviceCode) {
        const allBtn = document.getElementById('svc-all');
        if (allBtn) allBtn.classList.add('selected');
    } else {
        const el = document.getElementById(`svc-${serviceCode}`);
        if (el) el.classList.add('selected');
    }

    performSearch();
}

function selectCityChip(city) {
    const input = document.getElementById('location-input');
    input.value = city;
    
    document.querySelectorAll('.city-chip').forEach(chip => {
        if (chip.textContent.trim().toLowerCase() === city.toLowerCase()) {
            chip.classList.add('active');
        } else {
            chip.classList.remove('active');
        }
    });

    performSearch();
}

async function handleSearchSubmit(e) {
    if (e) e.preventDefault();
    performSearch();
}

async function performSearch() {
    const locInput = document.getElementById('location-input');
    const locationVal = locInput ? locInput.value.trim() : '';
    
    const resultsContainer = document.getElementById('worker-results-grid');
    const emptyState = document.getElementById('search-empty-state');
    const resultsCountEl = document.getElementById('results-count-text');

    resultsContainer.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);">
            <i class="fa-solid fa-spinner fa-spin" style="font-size: 1.5rem; color: var(--primary);"></i>
            <p style="margin-top: 0.5rem;">Finding matching local professionals...</p>
        </div>
    `;
    emptyState.style.display = 'none';

    try {
        const queryParams = new URLSearchParams();
        if (selectedService) queryParams.set('service', selectedService);
        if (locationVal) queryParams.set('location', locationVal);

        const res = await fetch(`/api/search?${queryParams.toString()}`);
        if (!res.ok) {
            throw new Error('Search failed to retrieve results.');
        }

        const workers = await res.json();

        if (resultsCountEl) {
            const svcName = selectedService ? (availableServices.find(s => s.code === selectedService)?.displayName || selectedService) : 'All Services';
            const locText = locationVal ? ` in "${locationVal}"` : '';
            resultsCountEl.innerHTML = `Showing <strong>${workers.length}</strong> available professionals for <strong>${escapeHtml(svcName)}</strong>${escapeHtml(locText)}`;
        }

        if (!workers || workers.length === 0) {
            resultsContainer.innerHTML = '';
            emptyState.style.display = 'block';
            return;
        }

        emptyState.style.display = 'none';
        resultsContainer.innerHTML = workers.map(w => `
            <div class="worker-card">
                <div>
                    <div class="worker-card-header">
                        <div class="worker-avatar">
                            <i class="fa-solid ${w.serviceIcon || 'fa-user-tie'}"></i>
                        </div>
                        <div class="worker-details">
                            <h4>${escapeHtml(w.name)}</h4>
                            <div class="verified-badge">
                                <i class="fa-solid fa-circle-check"></i>
                                <span>Verified Worker</span>
                            </div>
                        </div>
                    </div>

                    <div class="worker-meta" style="margin-top: 1rem;">
                        <span class="badge badge-service">
                            <i class="fa-solid ${w.serviceIcon || 'fa-wrench'}"></i>
                            ${escapeHtml(w.serviceDisplayName || w.service || 'Service')}
                        </span>
                        <span class="badge badge-location">
                            <i class="fa-solid fa-location-dot"></i>
                            ${escapeHtml(w.location || 'Location Not Listed')}
                        </span>
                    </div>
                </div>

                <div>
                    <div class="privacy-notice" style="margin-bottom: 0.85rem;">
                        <i class="fa-solid fa-shield-halved" style="color: #34d399;"></i>
                        <span>Identity & Credentials Verified by Admin</span>
                    </div>

                    <a href="tel:${escapeHtml(w.phone)}" class="btn-call">
                        <i class="fa-solid fa-phone"></i>
                        <span>Call ${escapeHtml(w.phone)}</span>
                    </a>
                </div>
            </div>
        `).join('');

    } catch (err) {
        showToast(err.message, 'error');
        resultsContainer.innerHTML = '';
        emptyState.style.display = 'block';
    }
}
