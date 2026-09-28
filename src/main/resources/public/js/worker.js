/**
 * Worker Portal Logic
 */

let currentWorkerData = null;

document.addEventListener('DOMContentLoaded', async () => {
    if (!Auth.requireRole('WORKER')) return;
    Auth.initNavbar('WORKER');

    await loadWorkerProfile();
});

async function loadWorkerProfile() {
    try {
        const token = Auth.getToken();
        const res = await fetch('/api/worker/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.error || 'Failed to load profile');
        }

        currentWorkerData = await res.json();
        populateProfile(currentWorkerData);

    } catch (err) {
        showToast(err.message, 'error');
    }
}

function populateProfile(data) {
    // 1. Update Preview Card
    document.getElementById('preview-worker-name').textContent = data.name || 'Unnamed Worker';
    document.getElementById('preview-worker-service').textContent = data.serviceDisplayName || data.service || 'Service Worker';
    document.getElementById('preview-worker-location').textContent = data.location || 'Location Not Set';
    document.getElementById('preview-worker-phone').textContent = data.phone || 'Phone Not Set';
    document.getElementById('preview-worker-aadhar').textContent = formatAadhar(data.aadhar);

    const iconEl = document.getElementById('preview-service-icon');
    if (iconEl) {
        iconEl.className = `fa-solid ${data.serviceIcon || 'fa-briefcase'}`;
    }

    // 2. Populate Form
    document.getElementById('worker-name').value = data.name || '';
    document.getElementById('worker-phone').value = data.phone || '';
    document.getElementById('worker-aadhar').value = data.aadhar || '';
    document.getElementById('worker-location').value = data.location || '';
    
    if (data.service) {
        document.getElementById('worker-service').value = data.service;
    }

    // Update Aadhar character count
    updateAadharCount();
}

function updateAadharCount() {
    const input = document.getElementById('worker-aadhar');
    const counter = document.getElementById('aadhar-count-hint');
    const val = input.value.trim();
    if (counter) {
        counter.textContent = `${val.length}/12 digits`;
        if (val.length === 12 && /^\d{12}$/.test(val)) {
            counter.style.color = '#34d399';
        } else {
            counter.style.color = '#f59e0b';
        }
    }
}

async function handleProfileUpdate(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-save-profile');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving Changes...';

    const name = document.getElementById('worker-name').value.trim();
    const phone = document.getElementById('worker-phone').value.trim();
    const aadhar = document.getElementById('worker-aadhar').value.trim();
    const location = document.getElementById('worker-location').value.trim();
    const service = document.getElementById('worker-service').value;

    // Frontend Validations
    if (!name) {
        showToast('Name is mandatory', 'error');
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> <span>Save Profile Updates</span>';
        return;
    }
    if (!location) {
        showToast('Location is mandatory for workers', 'error');
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> <span>Save Profile Updates</span>';
        return;
    }
    if (!/^\d{12}$/.test(aadhar)) {
        showToast('Aadhar number must be exactly 12 numeric digits', 'error');
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> <span>Save Profile Updates</span>';
        return;
    }
    if (!/^\d{10}$/.test(phone)) {
        showToast('Phone number must be exactly 10 digits', 'error');
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> <span>Save Profile Updates</span>';
        return;
    }

    try {
        const token = Auth.getToken();
        const res = await fetch('/api/worker/me', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ name, phone, aadhar, location, service })
        });

        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.error || 'Failed to update profile');
        }

        currentWorkerData = data;
        populateProfile(data);
        
        // Update session name if changed
        sessionStorage.setItem(Auth.NAME_KEY, name);
        Auth.initNavbar('WORKER');

        showToast('Profile updated and saved to data/users.json!', 'success');

    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> <span>Save Profile Updates</span>';
    }
}

function formatAadhar(aadhar) {
    if (!aadhar || aadhar.length !== 12) return aadhar || 'Not Set';
    return `${aadhar.slice(0, 4)} ${aadhar.slice(4, 8)} ${aadhar.slice(8, 12)}`;
}
