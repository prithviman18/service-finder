/**
 * Admin Portal Logic
 */

let allWorkers = [];
let allUsers = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!Auth.requireRole('ADMIN')) return;
    Auth.initNavbar('ADMIN');

    await loadAdminData();
});

async function loadAdminData() {
    try {
        const token = Auth.getToken();
        const headers = { 'Authorization': `Bearer ${token}` };

        // 1. Fetch Stats
        const statsRes = await fetch('/api/admin/stats', { headers });
        if (statsRes.ok) {
            const stats = await statsRes.json();
            renderStats(stats);
        }

        // 2. Fetch Workers
        const workersRes = await fetch('/api/admin/workers', { headers });
        if (workersRes.ok) {
            allWorkers = await workersRes.json();
            renderWorkersTable(allWorkers);
        }

        // 3. Fetch Users
        const usersRes = await fetch('/api/admin/users', { headers });
        if (usersRes.ok) {
            allUsers = await usersRes.json();
            renderUsersTable(allUsers);
        }

    } catch (err) {
        showToast('Error loading admin records: ' + err.message, 'error');
    }
}

function renderStats(stats) {
    document.getElementById('stat-total-workers').textContent = stats.totalWorkers || 0;
    document.getElementById('stat-total-users').textContent = stats.totalUsers || 0;
    document.getElementById('stat-total-accounts').textContent = stats.totalAccounts || 0;
    
    const serviceTypesCount = stats.serviceBreakdown ? Object.keys(stats.serviceBreakdown).length : 0;
    document.getElementById('stat-total-services').textContent = serviceTypesCount;
}

function renderWorkersTable(workers) {
    const tbody = document.getElementById('workers-table-body');
    const emptyState = document.getElementById('workers-empty-state');

    if (!workers || workers.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    tbody.innerHTML = workers.map((w, idx) => `
        <tr>
            <td style="color: var(--text-dim); font-size: 0.8rem;">#${idx + 1}</td>
            <td>
                <div style="font-weight: 700;">${escapeHtml(w.name)}</div>
                <div style="font-size: 0.75rem; color: var(--text-dim);">@${escapeHtml(w.username)}</div>
            </td>
            <td>
                <span class="badge badge-service">
                    <i class="fa-solid ${w.serviceIcon || 'fa-wrench'}"></i>
                    ${escapeHtml(w.serviceDisplayName || w.service || 'N/A')}
                </span>
            </td>
            <td>
                <span class="badge badge-location">
                    <i class="fa-solid fa-location-dot"></i>
                    ${escapeHtml(w.location || 'N/A')}
                </span>
            </td>
            <td>
                <a href="tel:${escapeHtml(w.phone)}" style="color: #38bdf8; text-decoration: none; font-weight: 600;">
                    <i class="fa-solid fa-phone" style="font-size: 0.75rem;"></i> ${escapeHtml(w.phone || 'N/A')}
                </a>
            </td>
            <td>
                <span class="badge badge-aadhar" title="Admin Verified Aadhar">
                    <i class="fa-solid fa-id-card"></i> ${formatAadhar(w.aadhar)}
                </span>
            </td>
        </tr>
    `).join('');
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-table-body');
    const emptyState = document.getElementById('users-empty-state');

    if (!users || users.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    tbody.innerHTML = users.map((u, idx) => `
        <tr>
            <td style="color: var(--text-dim); font-size: 0.8rem;">#${idx + 1}</td>
            <td>
                <div style="font-weight: 700;">${escapeHtml(u.name)}</div>
                <div style="font-size: 0.75rem; color: var(--text-dim);">@${escapeHtml(u.username)}</div>
            </td>
            <td>
                <a href="tel:${escapeHtml(u.phone)}" style="color: #38bdf8; text-decoration: none; font-weight: 600;">
                    <i class="fa-solid fa-phone" style="font-size: 0.75rem;"></i> ${escapeHtml(u.phone || 'N/A')}
                </a>
            </td>
            <td>
                <span class="badge" style="background: rgba(6, 182, 212, 0.15); color: #38bdf8;">
                    <i class="fa-solid fa-user-check"></i> Registered Customer
                </span>
            </td>
        </tr>
    `).join('');
}

function formatAadhar(aadhar) {
    if (!aadhar || aadhar.length !== 12) return aadhar || 'N/A';
    return `${aadhar.slice(0, 4)} ${aadhar.slice(4, 8)} ${aadhar.slice(8, 12)}`;
}

function filterWorkers() {
    const query = document.getElementById('worker-search-input').value.toLowerCase().trim();
    if (!query) {
        renderWorkersTable(allWorkers);
        return;
    }
    const filtered = allWorkers.filter(w => 
        (w.name && w.name.toLowerCase().includes(query)) ||
        (w.location && w.location.toLowerCase().includes(query)) ||
        (w.service && w.service.toLowerCase().includes(query)) ||
        (w.serviceDisplayName && w.serviceDisplayName.toLowerCase().includes(query)) ||
        (w.phone && w.phone.includes(query)) ||
        (w.aadhar && w.aadhar.includes(query))
    );
    renderWorkersTable(filtered);
}

function filterUsers() {
    const query = document.getElementById('user-search-input').value.toLowerCase().trim();
    if (!query) {
        renderUsersTable(allUsers);
        return;
    }
    const filtered = allUsers.filter(u => 
        (u.name && u.name.toLowerCase().includes(query)) ||
        (u.username && u.username.toLowerCase().includes(query)) ||
        (u.phone && u.phone.includes(query))
    );
    renderUsersTable(filtered);
}

function switchAdminTab(tab) {
    const workersSection = document.getElementById('section-workers');
    const usersSection = document.getElementById('section-users');
    const tabWorkersBtn = document.getElementById('tab-workers-btn');
    const tabUsersBtn = document.getElementById('tab-users-btn');

    if (tab === 'workers') {
        workersSection.style.display = 'block';
        usersSection.style.display = 'none';
        tabWorkersBtn.classList.add('active');
        tabUsersBtn.classList.remove('active');
    } else {
        workersSection.style.display = 'none';
        usersSection.style.display = 'block';
        tabWorkersBtn.classList.remove('active');
        tabUsersBtn.classList.add('active');
    }
}
