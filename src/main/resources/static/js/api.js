/* ===== API SERVICE ===== */
const API_BASE = '/api';

const api = {
    getToken: () => localStorage.getItem('token'),
    getUser: () => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); }
        catch { return {}; }
    },
    isAdmin: () => api.getUser().role === 'ADMIN',

    async request(method, endpoint, body = null) {
        const headers = { 'Content-Type': 'application/json' };
        const token = api.getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const opts = { method, headers };
        if (body) opts.body = JSON.stringify(body);

        let resp;
        try {
            resp = await fetch(API_BASE + endpoint, opts);
        } catch (networkErr) {
            throw new Error('Network error — is the server running?');
        }

        let data = {};
        try { data = await resp.json(); } catch(e) {}

        if (!resp.ok) {
            if (resp.status === 401) { api.logout(); throw new Error('Session expired.'); }
            if (resp.status === 403) throw new Error(data.message || 'Access denied.');
            if (data.errors) throw new Error(Object.values(data.errors).join(', '));
            throw new Error(data.message || `Request failed (${resp.status})`);
        }
        return data;
    },

    get: (ep) => api.request('GET', ep),
    post: (ep, body) => api.request('POST', ep, body),
    put: (ep, body) => api.request('PUT', ep, body),
    patch: (ep, body) => api.request('PATCH', ep, body),
    delete: (ep) => api.request('DELETE', ep),

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/pages/login.html';
    },

    requireAuth() {
        const token = api.getToken();
        if (!token) window.location.href = '/pages/login.html';
        return api.getUser();
    }
};

/* ===== HELPER FUNCTIONS ===== */
function getStatusBadge(status) {
    const map = {
        'TODO': '<span class="badge badge-todo">To Do</span>',
        'IN_PROGRESS': '<span class="badge badge-in-progress">In Progress</span>',
        'IN_REVIEW': '<span class="badge badge-in-review">In Review</span>',
        'DONE': '<span class="badge badge-done">Done</span>'
    };
    return map[status] || status;
}

function getPriorityBadge(priority) {
    const map = {
        'LOW': '<span class="badge badge-low">Low</span>',
        'MEDIUM': '<span class="badge badge-medium">Medium</span>',
        'HIGH': '<span class="badge badge-high">High</span>',
        'CRITICAL': '<span class="badge badge-critical">Critical</span>'
    };
    return map[priority] || priority;
}

function getRoleBadge(role) {
    return role === 'ADMIN'
        ? '<span class="badge badge-admin">Admin</span>'
        : '<span class="badge badge-member">Member</span>';
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function isOverdue(dueDate, status) {
    if (!dueDate || status === 'DONE') return false;
    return new Date(dueDate) < new Date();
}

function getInitials(name) {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

function showAlert(id, msg, type = 'error') {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = `alert alert-${type}`;
    el.textContent = msg;
    el.classList.remove('hidden');
    if (type === 'success') setTimeout(() => el.classList.add('hidden'), 3000);
}

function hideAlert(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('hidden');
}

function setLoading(btnId, loading, text = 'Save') {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    if (loading) {
        btn.disabled = true;
        btn.innerHTML = '<div class="spinner"></div>';
    } else {
        btn.disabled = false;
        btn.textContent = text;
    }
}

function renderSidebar(activePage) {
    const user = api.getUser();
    const isAdmin = user.role === 'ADMIN';

    return `
    <div class="sidebar" id="sidebar">
        <div class="sidebar-logo">
            <svg viewBox="0 0 32 32" fill="none">
                <rect width="32" height="32" rx="8" fill="url(#sg)"/>
                <path d="M8 12h16M8 16h10M8 20h13" stroke="white" stroke-width="2" stroke-linecap="round"/>
                <circle cx="22" cy="20" r="4" fill="white" opacity="0.9"/>
                <defs><linearGradient id="sg" x1="0" y1="0" x2="32" y2="32"><stop stop-color="#6366f1"/><stop offset="1" stop-color="#8b5cf6"/></linearGradient></defs>
            </svg>
            <span>TaskFlow</span>
        </div>

        <span class="sidebar-section-label">Main</span>
        <a href="/pages/dashboard.html" class="nav-item ${activePage === 'dashboard' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
            Dashboard
        </a>
        <a href="/pages/projects.html" class="nav-item ${activePage === 'projects' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
            Projects
        </a>
        <a href="/pages/tasks.html" class="nav-item ${activePage === 'tasks' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            My Tasks
        </a>
        ${isAdmin ? `
        <span class="sidebar-section-label">Admin</span>
        <a href="/pages/users.html" class="nav-item ${activePage === 'users' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            Users
        </a>` : ''}

        <div class="sidebar-spacer"></div>

        <div class="sidebar-user">
            <div class="user-avatar">${getInitials(user.fullName)}</div>
            <div class="user-info">
                <div class="user-name">${user.fullName || 'User'}</div>
                <div class="user-role">${user.role || 'Member'}</div>
            </div>
            <button class="logout-btn" onclick="api.logout()" title="Logout">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
            </button>
        </div>
    </div>`;
}
