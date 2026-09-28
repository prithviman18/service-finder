/**
 * Shared Authentication & Session Management Module
 */

// Toast Notifications Helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'fa-circle-info';
    if (type === 'success') iconClass = 'fa-circle-check';
    if (type === 'error') iconClass = 'fa-circle-exclamation';
    
    toast.innerHTML = `<i class="fa-solid ${iconClass}"></i> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Session Management
const Auth = {
    TOKEN_KEY: 'service_finder_token',
    ROLE_KEY: 'service_finder_role',
    NAME_KEY: 'service_finder_name',
    USERNAME_KEY: 'service_finder_username',

    saveSession(data) {
        if (data.token) sessionStorage.setItem(this.TOKEN_KEY, data.token);
        if (data.role) sessionStorage.setItem(this.ROLE_KEY, data.role);
        if (data.name) sessionStorage.setItem(this.NAME_KEY, data.name);
        if (data.username) sessionStorage.setItem(this.USERNAME_KEY, data.username);
    },

    getToken() {
        return sessionStorage.getItem(this.TOKEN_KEY);
    },

    getRole() {
        return sessionStorage.getItem(this.ROLE_KEY);
    },

    getName() {
        return sessionStorage.getItem(this.NAME_KEY) || 'User';
    },

    getUsername() {
        return sessionStorage.getItem(this.USERNAME_KEY);
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    clearSession() {
        sessionStorage.removeItem(this.TOKEN_KEY);
        sessionStorage.removeItem(this.ROLE_KEY);
        sessionStorage.removeItem(this.NAME_KEY);
        sessionStorage.removeItem(this.USERNAME_KEY);
    },

    async logout() {
        const token = this.getToken();
        if (token) {
            try {
                await fetch('/api/logout', {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` }
                });
            } catch (e) {
                console.warn('Logout request failed:', e);
            }
        }
        this.clearSession();
        window.location.href = '/login.html';
    },

    requireRole(expectedRole) {
        const token = this.getToken();
        const role = this.getRole();

        if (!token || !role) {
            this.clearSession();
            window.location.href = '/login.html';
            return false;
        }

        if (expectedRole && role !== expectedRole) {
            showToast(`Unauthorized. Required role: ${expectedRole}`, 'error');
            // Redirect to appropriate dashboard
            if (role === 'ADMIN') window.location.href = '/admin.html';
            else if (role === 'WORKER') window.location.href = '/worker.html';
            else window.location.href = '/user.html';
            return false;
        }

        return true;
    },

    initNavbar(roleDisplayName) {
        const bannerElement = document.getElementById('role-banner');
        const nameElement = document.getElementById('user-display-name');
        const roleBadge = document.getElementById('role-badge');
        const logoutBtn = document.getElementById('btn-logout');

        const role = this.getRole();
        const name = this.getName();

        if (nameElement) nameElement.textContent = name;
        if (roleBadge) {
            roleBadge.textContent = role || 'GUEST';
            roleBadge.className = `role-badge ${(role || '').toLowerCase()}`;
        }
        if (bannerElement) {
            bannerElement.innerHTML = `<i class="fa-solid fa-user-shield"></i> Logged in as: <strong>${escapeHtml(role)}</strong> (${escapeHtml(name)})`;
        }

        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                Auth.logout();
            });
        }
    }
};
