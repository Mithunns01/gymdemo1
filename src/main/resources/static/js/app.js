// ============================================================
// Gym Management System - Frontend Application
// ============================================================

const API_BASE = '';

// ========== Utility Functions ==========
function $(id) { return document.getElementById(id); }
function qs(sel) { return document.querySelector(sel); }
function qsa(sel) { return document.querySelectorAll(sel); }

function getToken() { return localStorage.getItem('token'); }
function setToken(t) { localStorage.setItem('token', t); }
function getUser() { return JSON.parse(localStorage.getItem('user') || '{}'); }
function setUser(u) { localStorage.setItem('user', JSON.stringify(u)); }
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    location.reload();
}

function getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
}

// ========== Toast Notifications ==========
function showToast(message, type = 'info') {
    const container = $('toastContainer');
    if (!container) return;
    const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type] || ''}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ========== API Calls ==========
async function apiCall(url, method = 'GET', body = null) {
    try {
        const options = { method, headers: getAuthHeaders() };
        if (body) options.body = JSON.stringify(body);
        const res = await fetch(url, options);
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Request failed');
        return data;
    } catch (err) {
        if (err.message.includes('401') || err.message.includes('Unauthorized') || err.message.includes('JWT')) {
            logout();
            showToast('Session expired. Please login again.', 'error');
        }
        throw err;
    }
}

// ========== Page Router ==========
function showPage(page, params = {}) {
    qsa('.page').forEach(p => p.classList.remove('active'));
    const target = $(page);
    if (target) {
        target.classList.add('active');
        window.scrollTo(0, 0);
    }

    qsa('.nav-item').forEach(n => n.classList.remove('active'));
    const navMap = {
        'dashboard-page': 'nav-dashboard',
        'members-page': 'nav-members',
        'trainers-page': 'nav-trainers',
        'plans-page': 'nav-plans',
        'attendance-page': 'nav-attendance',
        'payments-page': 'nav-payments',
        'workouts-page': 'nav-workouts',
        'profile-page': 'nav-profile',
        'renewals-page': 'nav-renewals'
    };
    const navEl = $(navMap[page] || 'nav-dashboard');
    if (navEl) navEl.classList.add('active');

    if (page === 'dashboard-page') initDashboard();
    else if (page === 'members-page') initMembers();
    else if (page === 'trainers-page') initTrainers();
    else if (page === 'plans-page') initPlans();
    else if (page === 'attendance-page') initAttendance();
    else if (page === 'payments-page') initPayments();
    else if (page === 'workouts-page') initWorkouts();
    else if (page === 'profile-page') initProfile();
    else if (page === 'renewals-page') initRenewals();
}

// ========== Login ==========
function initLogin() {
    const form = $('loginForm');
    const errorEl = $('loginError');

    qsa('.quick-login').forEach(btn => {
        btn.onclick = () => {
            const [u, p] = btn.dataset.creds.split('/');
            $('loginUsername').value = u;
            $('loginPassword').value = p;
            form.requestSubmit();
        };
    });

    form.onsubmit = async (e) => {
        e.preventDefault();
        const btn = form.querySelector('.btn');
        btn.disabled = true;
        btn.textContent = '⏳ Logging in...';
        errorEl.textContent = '';

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: $('loginUsername').value,
                    password: $('loginPassword').value
                })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Login failed');

const loginData = data.data;
            const token = loginData.token;
            const userId = loginData.userId;
            const username = loginData.username;
            const name = loginData.name;
            const role = loginData.role;
            const email = loginData.email;
            setToken(token);
            setUser({ userId, username, name, role, email });
            showToast(`Welcome, ${name}!`, 'success');
            initApp();
        } catch (err) {
            errorEl.textContent = err.message;
            showToast(err.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = '🔑 Login';
        }
    };
}

// ========== App Initialization ==========
function initApp() {
    const user = getUser();
    if (!user || !user.role) { showPage('login'); return; }

    qsa('.page').forEach(p => p.classList.remove('active'));
    $('app').classList.remove('hidden');
    $('loginPage').classList.add('hidden');

    const sid = $('sidebarUser');
    if (sid) {
        sid.innerHTML = `
            <div class="avatar">${user.name.charAt(0).toUpperCase()}</div>
            <div class="user-info">
                <div class="name">${user.name}</div>
                <div class="role">${user.role}</div>
            </div>
        `;
    }

    const role = user.role;
    qsa('.role-based').forEach(el => {
        const allowed = (el.dataset.roles || '').split(',');
        el.style.display = allowed.includes(role) ? '' : 'none';
    });

    showPage('dashboard-page');
}

// ========== Dashboard ==========
async function initDashboard() {
    const container = $('dashboardContent');
    container.innerHTML = '<div class="spinner"></div>';
    try {
        let stats;
        try {
            const res = await apiCall(`${API_BASE}/admin/dashboard`);
            stats = res.data;
        } catch {
            const res = await apiCall(`${API_BASE}/dashboard`);
            stats = res.data;
        }

        const user = getUser();
        let extraHtml = '';

        if (user.role === 'MEMBER') {
            try {
                const memRes = await apiCall(`${API_BASE}/members/${user.userId}`);
                const member = memRes.data;
                const memshipRes = await apiCall(`${API_BASE}/members/${user.userId}/membership`);
                const memship = memshipRes.data;
                extraHtml = `
                    <div class="card" style="margin-bottom:24px">
                        <div class="card-header"><h3>👤 My Profile</h3></div>
                        <div class="detail-grid">
                            <div class="detail-item"><div class="label">Name</div><div class="value">${member.user?.name || '-'}</div></div>
                            <div class="detail-item"><div class="label">Email</div><div class="value">${member.user?.email || '-'}</div></div>
                            <div class="detail-item"><div class="label">Phone</div><div class="value">${member.user?.phone || '-'}</div></div>
                            <div class="detail-item"><div class="label">Height</div><div class="value">${member.height || '-'} cm</div></div>
                            <div class="detail-item"><div class="label">Weight</div><div class="value">${member.weight || '-'} kg</div></div>
                            <div class="detail-item"><div class="label">BMI</div><div class="value">${member.bmi || '-'}</div></div>
                            <div class="detail-item"><div class="label">Trainer</div><div class="value">${member.assignedTrainer?.user?.name || 'Not assigned'}</div></div>
                            <div class="detail-item"><div class="label">Membership</div><div class="value">${memship?.plan?.name || 'None'}</div></div>
                        </div>
                    </div>
                `;
            } catch(e) {}
        }

        const statsItems = [
            { icon: '👥', label: 'Total Members', value: stats.totalMembers || 0, color: 'purple' },
            { icon: '✅', label: 'Active Memberships', value: stats.activeMembers || 0, color: 'green' },
            { icon: '⏰', label: "Today's Attendance", value: stats.todayAttendance || 0, color: 'blue' },
            { icon: '💰', label: 'Monthly Revenue', value: stats.monthlyRevenue ? '₹' + Number(stats.monthlyRevenue).toLocaleString() : '₹0', color: 'orange' },
            { icon: '👨‍🏫', label: 'Total Trainers', value: stats.totalTrainers || 0, color: 'yellow' },
            { icon: '⚠️', label: 'Expiring (this month)', value: stats.expiringThisMonth || 0, color: 'danger' },
        ];

        container.innerHTML = `
            ${extraHtml}
            <div class="stats-grid">
                ${statsItems.map(s => `
                    <div class="stat-card">
                        <div class="stat-icon ${s.color}">${s.icon}</div>
                        <div class="stat-info">
                            <h3>${s.value}</h3>
                            <p>${s.label}</p>
                        </div>
                    </div>
                `).join('')}
            </div>
            <div class="charts-grid">
                <div class="card">
                    <div class="card-header"><h3>📊 Monthly Revenue</h3></div>
                    <div class="chart-container"><canvas id="revenueChart"></canvas></div>
                </div>
                <div class="card">
                    <div class="card-header"><h3>📈 Top Active Members</h3></div>
                    <div class="chart-container"><canvas id="attendanceChart"></canvas></div>
                </div>
            </div>
        `;

        if (stats.monthlyRevenueChart && stats.monthlyRevenueChart.length) {
            drawRevenueChart(stats.monthlyRevenueChart);
        }
        if (stats.attendanceChart && stats.attendanceChart.length) {
            drawAttendanceChart(stats.attendanceChart);
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">📊</div><h3>Dashboard Data</h3><p>' + err.message + '</p></div>';
    }
}

function drawRevenueChart(data) {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const labels = data.map(d => months[(d.month || 1) - 1]);
    const values = data.map(d => parseFloat(d.revenue || 0));
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Revenue (₹)',
                data: values,
                backgroundColor: 'rgba(108, 92, 231, 0.5)',
                borderColor: 'rgba(108, 92, 231, 1)',
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { labels: { color: '#e0e0e0' } } },
            scales: {
                y: { beginAtZero: true, ticks: { color: '#888' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { ticks: { color: '#888' }, grid: { display: false } }
            }
        }
    });
}

function drawAttendanceChart(data) {
    const ctx = document.getElementById('attendanceChart');
    if (!ctx) return;
    const labels = data.map(d => d.name || 'Member ' + d.memberId);
    const values = data.map(d => d.count || 0);
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: ['#6c5ce7','#00b894','#0984e3','#e17055','#fdcb6e','#d63031','#a29bfe','#55efc4','#74b9ff','#ffeaa7']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: '#e0e0e0', font: { size: 11 } } }
            }
        }
    });
}

// ========== Members ==========
async function initMembers() {
    const container = $('membersContent');
    container.innerHTML = '<div class="spinner"></div>';

    try {
        let members;
        const user = getUser();
        
        if (user.role === 'TRAINER') {
            const trainerRes = await apiCall(`${API_BASE}/trainers/${user.userId}/members`);
            members = trainerRes.data || [];
        } else {
            const res = await apiCall(`${API_BASE}/members`);
            members = res.data || [];
        }

        container.innerHTML = `
            <div class="search-bar">
                <input type="text" id="memberSearch" placeholder="🔍 Search by name, phone, or username..." onkeyup="searchMembers(this.value)">
                ${user.role === 'ADMIN' ? '<button class="btn btn-primary" onclick="showAddMemberModal()">➕ Add Member</button>' : ''}
            </div>
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Gender</th>
                            <th>Trainer</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="membersTableBody">
                        ${members.length === 0 ? '<tr><td colspan="7"><div class="empty-state"><div class="icon">👥</div><h3>No members found</h3></div></td></tr>' :
                        members.map(m => `
                            <tr class="${m.active ? '' : 'status-expired'}">
                                <td><span class="table-img">${(m.user?.name || '?').charAt(0)}</span>${m.user?.name || 'N/A'}</td>
                                <td>${m.user?.email || '-'}</td>
                                <td>${m.user?.phone || '-'}</td>
                                <td>${m.gender || '-'}</td>
                                <td>${m.assignedTrainer?.user?.name || 'Not assigned'}</td>
                                <td><span class="status-badge ${m.active ? 'status-active' : 'status-inactive'}">${m.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                                <td>
                                    <div class="action-btns">
                                        <button class="btn btn-sm btn-info" onclick="viewMember(${m.id})">👁️</button>
                                        ${user.role === 'ADMIN' ? `
                                        <button class="btn btn-sm btn-secondary" onclick="editMember(${m.id})">✏️</button>
                                        <button class="btn btn-sm btn-danger" onclick="deleteMember(${m.id})">🗑️</button>
                                        ` : ''}
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">👥</div><h3>Error loading members</h3><p>' + err.message + '</p></div>';
    }
}

async function searchMembers(keyword) {
    if (!keyword || keyword.length < 2) { initMembers(); return; }
    const tbody = $('membersTableBody');
    if (!tbody) return;
    try {
        const res = await apiCall(`${API_BASE}/members/search?keyword=${encodeURIComponent(keyword)}`);
        const members = res.data || [];
        tbody.innerHTML = members.length === 0
            ? '<tr><td colspan="7"><div class="empty-state"><div class="icon">🔍</div><h3>No results found</h3></div></td></tr>'
            : members.map(m => `
                <tr>
                    <td><span class="table-img">${(m.user?.name || '?').charAt(0)}</span>${m.user?.name || 'N/A'}</td>
                    <td>${m.user?.email || '-'}</td>
                    <td>${m.user?.phone || '-'}</td>
                    <td>${m.gender || '-'}</td>
                    <td>${m.assignedTrainer?.user?.name || '-'}</td>
                    <td><span class="status-badge ${m.active ? 'status-active' : 'status-inactive'}">${m.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                    <td><button class="btn btn-sm btn-info" onclick="viewMember(${m.id})">👁️</button></td>
                </tr>
            `).join('');
    } catch(e) {
        tbody.innerHTML = '<tr><td colspan="7">Search failed</td></tr>';
    }
}

async function viewMember(id) {
    try {
        const res = await apiCall(`${API_BASE}/members/${id}`);
        const m = res.data;
        const memshipRes = await apiCall(`${API_BASE}/members/${id}/membership`);
        const memship = memshipRes.data;

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal" style="max-width:700px">
                <h2>👤 Member Details</h2>
                <div class="detail-grid">
                    <div class="detail-item"><div class="label">Name</div><div class="value">${m.user?.name || '-'}</div></div>
                    <div class="detail-item"><div class="label">Username</div><div class="value">${m.user?.username || '-'}</div></div>
                    <div class="detail-item"><div class="label">Email</div><div class="value">${m.user?.email || '-'}</div></div>
                    <div class="detail-item"><div class="label">Phone</div><div class="value">${m.user?.phone || '-'}</div></div>
                    <div class="detail-item"><div class="label">Gender</div><div class="value">${m.gender || '-'}</div></div>
                    <div class="detail-item"><div class="label">DOB</div><div class="value">${m.dateOfBirth || '-'}</div></div>
                    <div class="detail-item"><div class="label">Height</div><div class="value">${m.height || '-'} cm</div></div>
                    <div class="detail-item"><div class="label">Weight</div><div class="value">${m.weight || '-'} kg</div></div>
                    <div class="detail-item"><div class="label">BMI</div><div class="value">${m.bmi || '-'}</div></div>
                    <div class="detail-item"><div class="label">Emergency Contact</div><div class="value">${m.emergencyContact || '-'}</div></div>
                    <div class="detail-item"><div class="label">Trainer</div><div class="value">${m.assignedTrainer?.user?.name || 'Not assigned'}</div></div>
                    <div class="detail-item"><div class="label">Membership</div><div class="value">${memship?.plan?.name || 'None'}</div></div>
                    <div class="detail-item"><div class="label">Status</div><div class="value"><span class="status-badge ${m.active ? 'status-active' : 'status-inactive'}">${m.active ? 'ACTIVE' : 'INACTIVE'}</span></div></div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Close</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    } catch(err) {
        showToast(err.message, 'error');
    }
}

function showAddMemberModal() {
    showMemberFormModal(null, 'Add New Member');
}

async function editMember(id) {
    try {
        const res = await apiCall(`${API_BASE}/members/${id}`);
        showMemberFormModal(res.data, 'Edit Member');
    } catch(err) {
        showToast(err.message, 'error');
    }
}

async function showMemberFormModal(member, title) {
    let trainerOptions = '<option value="">No trainer</option>';
    try {
        const tres = await apiCall(`${API_BASE}/trainers`);
        (tres.data || []).forEach(t => {
            const selected = member?.assignedTrainer?.id === t.id ? 'selected' : '';
            trainerOptions += `<option value="${t.id}" ${selected}>${t.user?.name || t.id}</option>`;
        });
    } catch(e) {}

    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>${title}</h2>
            <form id="memberForm">
                <div class="form-row">
                    <div class="form-group">
                        <label>Name *</label>
                        <input type="text" id="mf_name" value="${member?.user?.name || ''}" required>
                    </div>
                    <div class="form-group">
                        <label>Username *</label>
                        <input type="text" id="mf_username" value="${member?.user?.username || ''}" ${member ? 'disabled' : 'required'}>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Email *</label>
                        <input type="email" id="mf_email" value="${member?.user?.email || ''}" required>
                    </div>
                    <div class="form-group">
                        <label>Phone</label>
                        <input type="text" id="mf_phone" value="${member?.user?.phone || ''}">
                    </div>
                </div>
                ${!member ? `
                <div class="form-row">
                    <div class="form-group">
                        <label>Password *</label>
                        <input type="password" id="mf_password" required>
                    </div>
                    <div class="form-group">
                        <label>Gender</label>
                        <select id="mf_gender">
                            <option value="">Select</option>
                            <option value="Male">Male</option>
                            <option value="Female">Female</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>
                </div>
                ` : `
                <div class="form-row">
                    <div class="form-group">
                        <label>Gender</label>
                        <select id="mf_gender">
                            <option value="">Select</option>
                            <option value="Male" ${member?.gender === 'Male' ? 'selected' : ''}>Male</option>
                            <option value="Female" ${member?.gender === 'Female' ? 'selected' : ''}>Female</option>
                            <option value="Other" ${member?.gender === 'Other' ? 'selected' : ''}>Other</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Trainer</label>
                        <select id="mf_trainer">${trainerOptions}</select>
                    </div>
                </div>
                `}
                <div class="form-row">
                    <div class="form-group">
                        <label>Height (cm)</label>
                        <input type="number" step="0.1" id="mf_height" value="${member?.height || ''}">
                    </div>
                    <div class="form-group">
                        <label>Weight (kg)</label>
                        <input type="number" step="0.1" id="mf_weight" value="${member?.weight || ''}">
                    </div>
                </div>
                <div class="form-group">
                    <label>Medical Conditions</label>
                    <textarea id="mf_medical">${member?.medicalConditions || ''}</textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">💾 Save</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('memberForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true;
        btn.textContent = '⏳ Saving...';

        try {
            const data = {
                name: $('mf_name').value,
                username: member ? member.user?.username : $('mf_username').value,
                ...(member ? {} : { password: $('mf_password').value }),
                email: $('mf_email').value,
                phone: $('mf_phone').value,
                gender: $('mf_gender')?.value || null,
                height: parseFloat($('mf_height')?.value) || null,
                weight: parseFloat($('mf_weight')?.value) || null,
                medicalConditions: $('mf_medical')?.value || null,
                assignedTrainerId: parseInt($('mf_trainer')?.value) || null
            };

            if (member) {
                await apiCall(`${API_BASE}/members/${member.id}`, 'PUT', data);
                showToast('Member updated successfully', 'success');
            } else {
                await apiCall(`${API_BASE}/auth/register`, 'POST', data);
                showToast('Member registered successfully', 'success');
            }
            modal.remove();
            initMembers();
        } catch(err) {
            showToast(err.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = '💾 Save';
        }
    };
}

async function deleteMember(id) {
    if (!confirm('Are you sure you want to deactivate this member?')) return;
    try {
        await apiCall(`${API_BASE}/members/${id}`, 'DELETE');
        showToast('Member deactivated', 'success');
        initMembers();
    } catch(err) {
        showToast(err.message, 'error');
    }
}

// ========== Trainers ==========
async function initTrainers() {
    const container = $('trainersContent');
    container.innerHTML = '<div class="spinner"></div>';
    try {
        const res = await apiCall(`${API_BASE}/trainers`);
        const trainers = res.data || [];
        const user = getUser();

        container.innerHTML = `
            <div class="page-header" style="border:none;margin-bottom:16px">
                <div></div>
                ${user.role === 'ADMIN' ? '<button class="btn btn-primary" onclick="showAddTrainerModal()">➕ Add Trainer</button>' : ''}
            </div>
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Specialization</th>
                            <th>Experience</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Members</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${trainers.length === 0 ? '<tr><td colspan="7"><div class="empty-state"><div class="icon">👨‍🏫</div><h3>No trainers found</h3></div></td></tr>' :
                        trainers.map(t => `
                            <tr>
                                <td><span class="table-img">${(t.user?.name || '?').charAt(0)}</span>${t.user?.name || 'N/A'}</td>
                                <td>${t.specialization || '-'}</td>
                                <td>${t.experience || 0} yrs</td>
                                <td>${t.user?.email || '-'}</td>
                                <td>${t.user?.phone || '-'}</td>
                                <td><button class="btn btn-sm btn-info" onclick="viewTrainerMembers(${t.id})">👥 View</button></td>
                                <td>
                                    <div class="action-btns">
                                        <button class="btn btn-sm btn-info" onclick="viewTrainer(${t.id})">👁️</button>
                                        ${user.role === 'ADMIN' ? `
                                        <button class="btn btn-sm btn-secondary" onclick="editTrainer(${t.id})">✏️</button>
                                        <button class="btn btn-sm btn-danger" onclick="deleteTrainer(${t.id})">🗑️</button>
                                        ` : ''}
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">👨‍🏫</div><h3>Error loading trainers</h3><p>' + err.message + '</p></div>';
    }
}

async function viewTrainer(id) {
    try {
        const res = await apiCall(`${API_BASE}/trainers/${id}`);
        const t = res.data;
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal" style="max-width:600px">
                <h2>👨‍🏫 Trainer Details</h2>
                <div class="detail-grid">
                    <div class="detail-item"><div class="label">Name</div><div class="value">${t.user?.name || '-'}</div></div>
                    <div class="detail-item"><div class="label">Specialization</div><div class="value">${t.specialization || '-'}</div></div>
                    <div class="detail-item"><div class="label">Experience</div><div class="value">${t.experience || 0} years</div></div>
                    <div class="detail-item"><div class="label">Email</div><div class="value">${t.user?.email || '-'}</div></div>
                    <div class="detail-item"><div class="label">Phone</div><div class="value">${t.user?.phone || '-'}</div></div>
                    <div class="detail-item"><div class="label">Bio</div><div class="value">${t.bio || 'No bio'}</div></div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Close</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    } catch(err) { showToast(err.message, 'error'); }
}

async function viewTrainerMembers(id) {
    try {
        const res = await apiCall(`${API_BASE}/trainers/${id}/members`);
        const members = res.data || [];
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal" style="max-width:600px">
                <h2>👥 Assigned Members</h2>
                ${members.length === 0 ? '<p style="color:var(--text-muted)">No assigned members</p>' : `
                <table>
                    <thead><tr><th>Name</th><th>Email</th><th>Phone</th></tr></thead>
                    <tbody>
                        ${members.map(m => '<tr><td>' + (m.user?.name || '-') + '</td><td>' + (m.user?.email || '-') + '</td><td>' + (m.user?.phone || '-') + '</td></tr>').join('')}
                    </tbody>
                </table>
                `}
                <div class="modal-footer">
                    <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Close</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    } catch(err) { showToast(err.message, 'error'); }
}

function showAddTrainerModal() { showTrainerFormModal(null, 'Add New Trainer'); }

async function editTrainer(id) {
    try {
        const res = await apiCall(`${API_BASE}/trainers/${id}`);
        showTrainerFormModal(res.data, 'Edit Trainer');
    } catch(err) { showToast(err.message, 'error'); }
}

function showTrainerFormModal(trainer, title) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>${title}</h2>
            <form id="trainerForm">
                <div class="form-row">
                    <div class="form-group">
                        <label>Name *</label>
                        <input type="text" id="tf_name" value="${trainer?.user?.name || ''}" required>
                    </div>
                    ${!trainer ? '<div class="form-group"><label>Username *</label><input type="text" id="tf_username" required></div>' : ''}
                </div>
                ${!trainer ? `
                <div class="form-row">
                    <div class="form-group"><label>Password *</label><input type="password" id="tf_password" required></div>
                    <div class="form-group"><label>Email *</label><input type="email" id="tf_email" required></div>
                </div>
                ` : ''}
                <div class="form-row">
                    <div class="form-group"><label>Specialization</label><input type="text" id="tf_spec" value="${trainer?.specialization || ''}"></div>
                    <div class="form-group"><label>Experience (years)</label><input type="number" id="tf_exp" value="${trainer?.experience || ''}"></div>
                </div>
                <div class="form-group"><label>Bio</label><textarea id="tf_bio">${trainer?.bio || ''}</textarea></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">💾 Save</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('trainerForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true;
        btn.textContent = '⏳ Saving...';
        try {
            const data = {
                name: $('tf_name').value,
                ...(trainer ? {} : { username: $('tf_username').value, password: $('tf_password').value, email: $('tf_email').value }),
                specialization: $('tf_spec').value,
                experience: parseInt($('tf_exp').value) || 0,
                bio: $('tf_bio').value
            };
            if (trainer) {
                await apiCall(`${API_BASE}/trainers/${trainer.id}`, 'PUT', data);
                showToast('Trainer updated', 'success');
            } else {
                await apiCall(`${API_BASE}/trainers`, 'POST', data);
                showToast('Trainer created', 'success');
            }
            modal.remove();
            initTrainers();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '💾 Save'; }
    };
}

async function deleteTrainer(id) {
    if (!confirm('Delete this trainer?')) return;
    try {
        await apiCall(`${API_BASE}/trainers/${id}`, 'DELETE');
        showToast('Trainer deleted', 'success');
        initTrainers();
    } catch(err) { showToast(err.message, 'error'); }
}

// ========== Plans ==========
async function initPlans() {
    const container = $('plansContent');
    container.innerHTML = '<div class="spinner"></div>';
    try {
        const res = await apiCall(`${API_BASE}/admin/plans`);
        const plans = res.data || [];

        container.innerHTML = `
            <div class="page-header" style="border:none;margin-bottom:16px">
                <div></div>
                <button class="btn btn-primary" onclick="showAddPlanModal()">➕ Add Plan</button>
            </div>
            <div class="stats-grid" style="grid-template-columns:repeat(auto-fit,minmax(280px,1fr))">
                ${plans.map(p => `
                    <div class="card" style="padding:24px">
                        <div style="display:flex;justify-content:space-between;align-items:start;margin-bottom:12px">
                            <h3 style="font-size:1.1em">${p.name}</h3>
                            <span class="status-badge ${p.active ? 'status-active' : 'status-inactive'}">${p.active ? 'ACTIVE' : 'INACTIVE'}</span>
                        </div>
                        <p style="color:var(--text-muted);font-size:13px;margin-bottom:16px">${p.description || 'No description'}</p>
                        <div style="display:flex;gap:16px;margin-bottom:16px">
                            <div><small style="color:var(--text-muted)">Duration</small><br><strong>${p.durationDays} days</strong></div>
                            <div><small style="color:var(--text-muted)">Price</small><br><strong style="color:var(--secondary)">₹${p.price}</strong></div>
                        </div>
                        <div class="action-btns">
                            <button class="btn btn-sm btn-secondary" onclick="editPlan(${p.id})">✏️ Edit</button>
                        </div>
                    </div>
                `).join('')}
                ${plans.length === 0 ? '<div class="empty-state" style="grid-column:1/-1"><div class="icon">📋</div><h3>No plans created</h3></div>' : ''}
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">📋</div><h3>Error</h3><p>' + err.message + '</p></div>';
    }
}

function showAddPlanModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>➕ Add Membership Plan</h2>
            <form id="planForm">
                <div class="form-group"><label>Plan Name *</label><input type="text" id="pf_name" required></div>
                <div class="form-group"><label>Description</label><textarea id="pf_desc"></textarea></div>
                <div class="form-row">
                    <div class="form-group"><label>Duration (days) *</label><input type="number" id="pf_duration" required min="1"></div>
                    <div class="form-group"><label>Price (₹) *</label><input type="number" step="0.01" id="pf_price" required min="0"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">💾 Create</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('planForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true; btn.textContent = '⏳ Creating...';
        try {
            await apiCall(`${API_BASE}/admin/plans`, 'POST', {
                name: $('pf_name').value,
                description: $('pf_desc').value,
                durationDays: parseInt($('pf_duration').value),
                price: parseFloat($('pf_price').value),
                active: true
            });
            showToast('Plan created', 'success');
            modal.remove();
            initPlans();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '💾 Create'; }
    };
}

async function editPlan(id) {
    try {
        const res = await apiCall(`${API_BASE}/admin/plans`);
        const plan = (res.data || []).find(p => p.id === id);
        if (!plan) throw new Error('Plan not found');

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal">
                <h2>✏️ Edit Plan</h2>
                <form id="planEditForm">
                    <div class="form-group"><label>Plan Name</label><input type="text" id="pe_name" value="${plan.name}" required></div>
                    <div class="form-group"><label>Description</label><textarea id="pe_desc">${plan.description || ''}</textarea></div>
                    <div class="form-row">
                        <div class="form-group"><label>Duration (days)</label><input type="number" id="pe_duration" value="${plan.durationDays}" required></div>
                        <div class="form-group"><label>Price (₹)</label><input type="number" step="0.01" id="pe_price" value="${plan.price}" required></div>
                    </div>
                    <div class="form-group"><label><input type="checkbox" id="pe_active" ${plan.active ? 'checked' : ''}> Active</label></div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                        <button type="submit" class="btn btn-primary">💾 Update</button>
                    </div>
                </form>
            </div>
        `;
        document.body.appendChild(modal);

        $('planEditForm').onsubmit = async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('.btn-primary');
            btn.disabled = true; btn.textContent = '⏳ Updating...';
            try {
                await apiCall(`${API_BASE}/admin/plans/${id}`, 'PUT', {
                    name: $('pe_name').value,
                    description: $('pe_desc').value,
                    durationDays: parseInt($('pe_duration').value),
                    price: parseFloat($('pe_price').value),
                    active: $('pe_active').checked
                });
                showToast('Plan updated', 'success');
                modal.remove();
                initPlans();
            } catch(err) { showToast(err.message, 'error'); }
            finally { btn.disabled = false; btn.textContent = '💾 Update'; }
        };
    } catch(err) { showToast(err.message, 'error'); }
}

// ========== Attendance ==========
async function initAttendance() {
    const container = $('attendanceContent');
    container.innerHTML = '<div class="spinner"></div>';
    const user = getUser();

    try {
        if (user.role === 'ADMIN' || user.role === 'TRAINER') {
            const dateInput = new Date().toISOString().split('T')[0];
            container.innerHTML = `
                <div class="search-bar" style="flex-wrap:wrap">
                    <input type="date" id="attDate" value="${dateInput}" onchange="loadAttendanceReport()">
                    ${user.role === 'ADMIN' ? '<button class="btn btn-primary" onclick="showMarkAttendanceModal()">➕ Mark Attendance</button>' : ''}
                    <a class="btn btn-info" href="${API_BASE}/export/attendance?date=${dateInput}" target="_blank">📥 Export Excel</a>
                </div>
                <div id="attendanceReport"></div>
            `;
            loadAttendanceReport();
        } else {
            const res = await apiCall(`${API_BASE}/members/${user.userId}/attendance`);
            const records = res.data || [];
            container.innerHTML = `
                <div class="search-bar">
                    <button class="btn btn-primary" onclick="markSelfAttendance()">✅ Mark Today's Attendance</button>
                </div>
                <div class="table-container">
                    <table>
                        <thead><tr><th>Date</th><th>Time</th><th>Status</th><th>Notes</th></tr></thead>
                        <tbody>
                            ${records.length === 0 ? '<tr><td colspan="4"><div class="empty-state"><div class="icon">📅</div><h3>No attendance records</h3></div></td></tr>' :
                            records.map(r => '<tr><td>' + (r.attendanceDate || '-') + '</td><td>' + (r.checkInTime || '-') + '</td><td><span class=\"status-badge status-active\">' + (r.status || 'PRESENT') + '</span></td><td>' + (r.notes || '-') + '</td></tr>').join('')}
                        </tbody>
                    </table>
                </div>
            `;
        }
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">📅</div><h3>Error</h3><p>' + err.message + '</p></div>';
    }
}

async function loadAttendanceReport() {
    const container = $('attendanceReport');
    if (!container) return;
    const date = $('attDate')?.value || new Date().toISOString().split('T')[0];
    container.innerHTML = '<div class="spinner"></div>';
    try {
        const res = await apiCall(`${API_BASE}/admin/attendance/daily?date=${date}`);
        const data = res.data || [];
        container.innerHTML = `
            <div class="table-container">
                <table>
                    <thead><tr><th>Name</th><th>Email</th><th>Check-in Time</th><th>Status</th></tr></thead>
                    <tbody>
                        ${data.length === 0 ? '<tr><td colspan="4"><div class="empty-state"><div class="icon">📅</div><h3>No attendance for this date</h3></div></td></tr>' :
                        data.map(r => '<tr><td>' + (r[0] || '-') + '</td><td>' + (r[1] || '-') + '</td><td>' + (r[2] || '-') + '</td><td><span class=\"status-badge status-active\">' + (r[3] || 'PRESENT') + '</span></td></tr>').join('')}
                    </tbody>
                </table>
            </div>
            <div style="margin-top:12px;display:flex;gap:10px">
                <a class="btn btn-info" href="${API_BASE}/export/attendance?date=${date}" target="_blank">📥 Export to Excel</a>
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<p style="color:var(--danger)">' + err.message + '</p>';
    }
}

function showMarkAttendanceModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>✅ Mark Attendance</h2>
            <form id="attForm">
                <div class="form-group"><label>Member ID</label><input type="number" id="af_memberId" placeholder="Enter member ID" required></div>
                <div class="form-group"><label>Date</label><input type="date" id="af_date" value="${new Date().toISOString().split('T')[0]}"></div>
                <div class="form-group"><label>Notes</label><textarea id="af_notes" placeholder="Optional notes"></textarea></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">✅ Mark</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('attForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true; btn.textContent = '⏳ Marking...';
        try {
            await apiCall(`${API_BASE}/admin/attendance`, 'POST', {
                memberId: parseInt($('af_memberId').value),
                attendanceDate: $('af_date').value || null,
                notes: $('af_notes').value || null
            });
            showToast('Attendance marked', 'success');
            modal.remove();
            loadAttendanceReport();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '✅ Mark'; }
    };
}

async function markSelfAttendance() {
    const user = getUser();
    if (!user.userId) return;
    try {
        await apiCall(`${API_BASE}/attendance`, 'POST', { memberId: user.userId });
        showToast('Attendance marked successfully!', 'success');
        initAttendance();
    } catch(err) { showToast(err.message, 'error'); }
}

// ========== Payments ==========
async function initPayments() {
    const container = $('paymentsContent');
    container.innerHTML = '<div class="spinner"></div>';
    const user = getUser();
    try {
        let payments;
        if (user.role === 'MEMBER') {
            const res = await apiCall(`${API_BASE}/members/${user.userId}/payments`);
            payments = res.data || [];
        } else {
            const res = await apiCall(`${API_BASE}/payments`);
            payments = res.data || [];
        }

        let revenueChartData = [];
        try {
            const revRes = await apiCall(`${API_BASE}/payments/report?year=${new Date().getFullYear()}`);
            revenueChartData = revRes.data || [];
        } catch(e) {}

        container.innerHTML = `
            <div class="page-header" style="border:none;margin-bottom:16px">
                <div></div>
                ${user.role === 'ADMIN' ? '<button class="btn btn-primary" onclick="showRecordPaymentModal()">💰 Record Payment</button>' : ''}
            </div>
            ${revenueChartData.length ? `
            <div class="card" style="margin-bottom:24px">
                <div class="card-header"><h3>📊 Monthly Revenue (${new Date().getFullYear()})</h3></div>
                <div class="chart-container" style="height:250px"><canvas id="paymentsRevenueChart"></canvas></div>
            </div>` : ''}
            <div class="table-container">
                <table>
                    <thead><tr><th>Member</th><th>Amount</th><th>Date</th><th>Mode</th><th>Transaction ID</th><th>Status</th></tr></thead>
                    <tbody>
                        ${payments.length === 0 ? '<tr><td colspan="6"><div class="empty-state"><div class="icon">💰</div><h3>No payments yet</h3></div></td></tr>' :
                        payments.map(p => '<tr><td>' + (p.member?.user?.name || p.memberId || '-') + '</td><td><strong style=\"color:var(--secondary)\">₹' + p.amount + '</strong></td><td>' + (p.paymentDate || '-') + '</td><td>' + (p.paymentMode || '-') + '</td><td style=\"font-size:12px\">' + (p.transactionId || '-') + '</td><td><span class=\"status-badge status-completed\">' + (p.status || 'COMPLETED') + '</span></td></tr>').join('')}
                    </tbody>
                </table>
            </div>
        `;

        if (revenueChartData.length) {
            setTimeout(() => drawPaymentsChart(revenueChartData), 100);
        }
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">💰</div><h3>Error</h3><p>' + err.message + '</p></div>';
    }
}

function drawPaymentsChart(data) {
    const ctx = document.getElementById('paymentsRevenueChart');
    if (!ctx) return;
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const labels = data.map(d => months[(d.month || 1) - 1]);
    const values = data.map(d => parseFloat(d.revenue || 0));
    new Chart(ctx, {
        type: 'line',
        data: { labels, datasets: [{ label: 'Revenue (₹)', data: values, borderColor: '#00b894', backgroundColor: 'rgba(0,184,148,0.1)', fill: true, tension: 0.4 }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { labels: { color: '#e0e0e0' } } },
            scales: {
                y: { beginAtZero: true, ticks: { color: '#888' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { ticks: { color: '#888' }, grid: { display: false } }
            }
        }
    });
}

function showRecordPaymentModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>💰 Record Payment</h2>
            <form id="paymentForm">
                <div class="form-group"><label>Member ID *</label><input type="number" id="pay_memberId" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Amount (₹) *</label><input type="number" step="0.01" id="pay_amount" required min="0"></div>
                    <div class="form-group"><label>Payment Mode *</label><select id="pay_mode" required>
                        <option value="CASH">Cash</option><option value="CARD">Card</option><option value="UPI">UPI</option><option value="BANK_TRANSFER">Bank Transfer</option>
                    </select></div>
                </div>
                <div class="form-group"><label>Transaction ID</label><input type="text" id="pay_txn" placeholder="Optional"></div>
                <div class="form-group"><label>Notes</label><textarea id="pay_notes"></textarea></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">💰 Record</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('paymentForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true; btn.textContent = '⏳ Recording...';
        try {
            await apiCall(`${API_BASE}/payments`, 'POST', {
                memberId: parseInt($('pay_memberId').value),
                amount: parseFloat($('pay_amount').value),
                paymentMode: $('pay_mode').value,
                transactionId: $('pay_txn').value || null,
                notes: $('pay_notes').value || null
            });
            showToast('Payment recorded', 'success');
            modal.remove();
            initPayments();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '💰 Record'; }
    };
}

// ========== Workouts ==========
async function initWorkouts() {
    const container = $('workoutsContent');
    container.innerHTML = '<div class="spinner"></div>';
    const user = getUser();

    try {
        let workouts = [];
        if (user.role === 'MEMBER') {
            const res = await apiCall(`${API_BASE}/members/${user.userId}/workouts`);
            workouts = res.data || [];
        } else if (user.role === 'TRAINER') {
            const res = await apiCall(`${API_BASE}/trainers/${user.userId}/workouts`);
            workouts = res.data || [];
        } else {
            const res = await apiCall(`${API_BASE}/trainers/1/workouts`);
            workouts = res.data || [];
        }

        container.innerHTML = `
            <div class="page-header" style="border:none;margin-bottom:16px">
                <div></div>
                ${user.role !== 'MEMBER' ? '<button class="btn btn-primary" onclick="showCreateWorkoutModal()">➕ Create Workout</button>' : ''}
            </div>
            <div class="table-container">
                <table>
                    <thead><tr><th>Title</th><th>Member</th><th>Trainer</th><th>Difficulty</th><th>Duration</th><th>Status</th><th>Actions</th></tr></thead>
                    <tbody>
                        ${workouts.length === 0 ? '<tr><td colspan="7"><div class="empty-state"><div class="icon">💪</div><h3>No workout plans</h3></div></td></tr>' :
                        workouts.map(w => '<tr><td><strong>' + (w.title || 'Untitled') + '</strong></td><td>' + (w.member?.user?.name || '-') + '</td><td>' + (w.trainer?.user?.name || '-') + '</td><td><span class=\"status-badge ' + (w.difficulty === 'BEGINNER' ? 'status-active' : w.difficulty === 'INTERMEDIATE' ? 'status-completed' : 'status-expired') + '\">' + (w.difficulty || '-') + '</span></td><td>' + (w.durationWeeks || '-') + ' weeks</td><td><span class=\"status-badge ' + (w.status === 'ACTIVE' ? 'status-active' : 'status-completed') + '\">' + (w.status || 'ACTIVE') + '</span></td><td><button class=\"btn btn-sm btn-info\" onclick=\"viewWorkout(' + w.id + ')\">👁️</button>' + (user.role !== 'MEMBER' ? '<button class=\"btn btn-sm btn-secondary\" onclick=\"editWorkout(' + w.id + ')\">✏️</button>' : '') + '</td></tr>').join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">💪</div><h3>Error</h3><p>' + err.message + '</p></div>';
    }
}

async function viewWorkout(id) {
    try {
        // Get workouts list and find the one
        const user = getUser();
        let workouts;
        if (user.role === 'TRAINER') {
            const res = await apiCall(`${API_BASE}/trainers/${user.userId}/workouts`);
            workouts = res.data || [];
        } else if (user.role === 'MEMBER') {
            const res = await apiCall(`${API_BASE}/members/${user.userId}/workouts`);
            workouts = res.data || [];
        } else {
            const res = await apiCall(`${API_BASE}/trainers/1/workouts`);
            workouts = res.data || [];
        }
        const w = workouts.find(x => x.id === id);
        if (!w) { showToast('Workout not found', 'error'); return; }

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal" style="max-width:600px">
                <h2>💪 ${w.title || 'Workout Plan'}</h2>
                <div class="detail-grid">
                    <div class="detail-item"><div class="label">Member</div><div class="value">${w.member?.user?.name || '-'}</div></div>
                    <div class="detail-item"><div class="label">Trainer</div><div class="value">${w.trainer?.user?.name || '-'}</div></div>
                    <div class="detail-item"><div class="label">Difficulty</div><div class="value">${w.difficulty || '-'}</div></div>
                    <div class="detail-item"><div class="label">Duration</div><div class="value">${w.durationWeeks || '-'} weeks</div></div>
                    <div class="detail-item"><div class="label">Status</div><div class="value">${w.status || 'ACTIVE'}</div></div>
                    <div class="detail-item" style="grid-column:1/-1"><div class="label">Exercises</div><div class="value" style="white-space:pre-wrap">${w.exercises || 'Not specified'}</div></div>
                    <div class="detail-item" style="grid-column:1/-1"><div class="label">Notes</div><div class="value" style="white-space:pre-wrap">${w.notes || 'No notes'}</div></div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Close</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    } catch(err) { showToast(err.message, 'error'); }
}

function showCreateWorkoutModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>➕ Create Workout Plan</h2>
            <form id="workoutForm">
                <div class="form-group"><label>Title *</label><input type="text" id="wf_title" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Member ID *</label><input type="number" id="wf_memberId" required></div>
                    <div class="form-group"><label>Difficulty</label><select id="wf_diff">
                        <option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option>
                    </select></div>
                </div>
                <div class="form-group"><label>Duration (weeks)</label><input type="number" id="wf_duration" value="4"></div>
                <div class="form-group"><label>Exercises *</label><textarea id="wf_exercises" rows="4" placeholder="List exercises with sets/reps..." required></textarea></div>
                <div class="form-group"><label>Notes</label><textarea id="wf_notes"></textarea></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">💾 Create</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('workoutForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true; btn.textContent = '⏳ Creating...';
        try {
            await apiCall(`${API_BASE}/trainers/workout`, 'POST', {
                memberId: parseInt($('wf_memberId').value),
                title: $('wf_title').value,
                exercises: $('wf_exercises').value,
                difficulty: $('wf_diff').value,
                durationWeeks: parseInt($('wf_duration').value) || null,
                notes: $('wf_notes').value || null
            });
            showToast('Workout plan created', 'success');
            modal.remove();
            initWorkouts();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '💾 Create'; }
    };
}

async function editWorkout(id) {
    try {
        const user = getUser();
        let workouts;
        if (user.role === 'TRAINER') {
            const res = await apiCall(`${API_BASE}/trainers/${user.userId}/workouts`);
            workouts = res.data || [];
        } else {
            const res = await apiCall(`${API_BASE}/trainers/1/workouts`);
            workouts = res.data || [];
        }
        const w = workouts.find(x => x.id === id);
        if (!w) { showToast('Workout not found', 'error'); return; }

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
        modal.innerHTML = `
            <div class="modal">
                <h2>✏️ Edit Workout</h2>
                <form id="workoutEditForm">
                    <div class="form-group"><label>Title</label><input type="text" id="we_title" value="${w.title || ''}" required></div>
                    <div class="form-row">
                        <div class="form-group"><label>Difficulty</label><select id="we_diff">
                            <option value="BEGINNER" ${w.difficulty === 'BEGINNER' ? 'selected' : ''}>Beginner</option>
                            <option value="INTERMEDIATE" ${w.difficulty === 'INTERMEDIATE' ? 'selected' : ''}>Intermediate</option>
                            <option value="ADVANCED" ${w.difficulty === 'ADVANCED' ? 'selected' : ''}>Advanced</option>
                        </select></div>
                        <div class="form-group"><label>Duration (weeks)</label><input type="number" id="we_duration" value="${w.durationWeeks || 4}"></div>
                    </div>
                    <div class="form-group"><label>Status</label><select id="we_status">
                        <option value="ACTIVE" ${w.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="COMPLETED" ${w.status === 'COMPLETED' ? 'selected' : ''}>Completed</option>
                    </select></div>
                    <div class="form-group"><label>Exercises</label><textarea id="we_exercises" rows="4">${w.exercises || ''}</textarea></div>
                    <div class="form-group"><label>Notes</label><textarea id="we_notes">${w.notes || ''}</textarea></div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                        <button type="submit" class="btn btn-primary">💾 Update</button>
                    </div>
                </form>
            </div>
        `;
        document.body.appendChild(modal);

        $('workoutEditForm').onsubmit = async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('.btn-primary');
            btn.disabled = true; btn.textContent = '⏳ Updating...';
            try {
                await apiCall(`${API_BASE}/trainers/workout/${id}`, 'PUT', {
                    title: $('we_title').value,
                    exercises: $('we_exercises').value,
                    difficulty: $('we_diff').value,
                    durationWeeks: parseInt($('we_duration').value) || null,
                    status: $('we_status').value,
                    notes: $('we_notes').value || null
                });
                showToast('Workout updated', 'success');
                modal.remove();
                initWorkouts();
            } catch(err) { showToast(err.message, 'error'); }
            finally { btn.disabled = false; btn.textContent = '💾 Update'; }
        };
    } catch(err) { showToast(err.message, 'error'); }
}

// ========== Profile ==========
async function initProfile() {
    const container = $('profileContent');
    container.innerHTML = '<div class="spinner"></div>';
    const user = getUser();

    try {
        let memberData = null;
        if (user.role === 'MEMBER') {
            const res = await apiCall(`${API_BASE}/members/${user.userId}`);
            memberData = res.data;
        }

        container.innerHTML = `
            <div class="profile-section">
                <div class="profile-card card">
                    <div class="avatar-lg" style="background:var(--primary);margin:0 auto 16px;width:80px;height:80px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:32px">${user.name.charAt(0)}</div>
                    <h2>${user.name}</h2>
                    <p style="color:var(--text-muted)">@${user.username}</p>
                    <span class="role-tag status-badge status-completed">${user.role}</span>
                    <div style="margin-top:20px">
                        <button class="btn btn-danger btn-sm" onclick="logout()">🚪 Logout</button>
                    </div>
                </div>
                <div class="card profile-details">
                    <h3 style="margin-bottom:16px">Account Details</h3>
                    <div class="detail-row"><span class="label">Username</span><span class="value">${user.username}</span></div>
                    <div class="detail-row"><span class="label">Name</span><span class="value">${user.name}</span></div>
                    <div class="detail-row"><span class="label">Role</span><span class="value"><span class="status-badge status-completed">${user.role}</span></span></div>
                    <div class="detail-row"><span class="label">User ID</span><span class="value">${user.userId}</span></div>
                </div>
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">👤</div><h3>Error loading profile</h3><p>' + err.message + '</p></div>';
    }
}

// ========== Renewals ==========
async function initRenewals() {
    const container = $('renewalsContent');
    container.innerHTML = '<div class="spinner"></div>';
    try {
        const expiringRes = await apiCall(`${API_BASE}/admin/members/expiring?days=30`);
        const expiring = expiringRes.data || [];

        const plansRes = await apiCall(`${API_BASE}/admin/plans`);
        const plans = plansRes.data || [];

        container.innerHTML = `
            <div class="page-header" style="border:none;margin-bottom:16px">
                <div></div>
                <button class="btn btn-primary" onclick="showRenewModal()">🔄 Renew Membership</button>
            </div>

            <div class="card" style="margin-bottom:24px">
                <div class="card-header"><h3>⚠️ Memberships Expiring in 30 Days</h3></div>
                <div class="table-container">
                    <table>
                        <thead><tr><th>Member</th><th>Plan</th><th>End Date</th><th>Days Left</th><th>Status</th></tr></thead>
                        <tbody>
                            ${expiring.length === 0 ? '<tr><td colspan="5"><div class="empty-state"><div class="icon">✅</div><h3>No memberships expiring soon</h3></div></td></tr>' :
                            expiring.map(e => '<tr><td>' + (e.memberName || '-') + '</td><td>' + (e.planName || '-') + '</td><td>' + (e.endDate || '-') + '</td><td>' + (e.daysRemaining || 0) + '</td><td><span class=\"status-badge ' + (e.daysRemaining <= 7 ? 'status-expired' : 'status-active') + '\">' + (e.daysRemaining <= 7 ? 'EXPIRING' : 'ACTIVE') + '</span></td></tr>').join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch(err) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🔄</div><h3>Error</h3><p>' + err.message + '</p></div>';
    }
}

function showRenewModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
    modal.innerHTML = `
        <div class="modal">
            <h2>🔄 Renew Membership</h2>
            <form id="renewForm">
                <div class="form-row">
                    <div class="form-group"><label>Member ID *</label><input type="number" id="rn_memberId" required></div>
                    <div class="form-group"><label>Plan ID *</label><input type="number" id="rn_planId" required></div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Payment Mode</label><select id="rn_paymode">
                        <option value="CASH">Cash</option><option value="CARD">Card</option><option value="UPI">UPI</option><option value="BANK_TRANSFER">Bank Transfer</option>
                    </select></div>
                    <div class="form-group"><label>Transaction ID</label><input type="text" id="rn_txn" placeholder="Optional"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                    <button type="submit" class="btn btn-primary">🔄 Renew</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    $('renewForm').onsubmit = async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('.btn-primary');
        btn.disabled = true; btn.textContent = '⏳ Processing...';
        try {
            await apiCall(`${API_BASE}/admin/renew`, 'POST', {
                memberId: parseInt($('rn_memberId').value),
                planId: parseInt($('rn_planId').value),
                paymentMode: $('rn_paymode').value,
                transactionId: $('rn_txn').value || null
            });
            showToast('Membership renewed successfully', 'success');
            modal.remove();
            initRenewals();
        } catch(err) { showToast(err.message, 'error'); }
        finally { btn.disabled = false; btn.textContent = '🔄 Renew'; }
    };
}

// ========== Initialize ==========
document.addEventListener('DOMContentLoaded', () => {
    if (getToken()) {
        initApp();
    } else {
        initLogin();
    }
});

