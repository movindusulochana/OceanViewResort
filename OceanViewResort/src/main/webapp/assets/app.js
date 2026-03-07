/**
 * OceanView Resort Management System - app.js
 * Single-Page Application controller using Vanilla JS + Fetch API.
 * No external JS libraries required.
 */
'use strict';

/* ============================================================
   CONFIGURATION
   ============================================================ */
const BASE = '';   // Context path (empty = same origin / Tomcat root context)

/* ============================================================
   UTILITY - Toast Notifications
   ============================================================ */
const Toast = {
    show(msg, type = 'info', duration = 3500) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        const icons = { success: '\u2713', error: '\u2715', info: '\u2139' };
        toast.innerHTML = `<span style="font-size:1.1rem">${icons[type] || ''}</span> ${msg}`;
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            toast.style.transition = '0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },
    success: (m) => Toast.show(m, 'success'),
    error:   (m) => Toast.show(m, 'error', 4500),
    info:    (m) => Toast.show(m, 'info'),
};

/* ============================================================
   UTILITY - HTTP helpers
   ============================================================ */
async function apiFetch(url, options = {}) {
    const response = await fetch(BASE + url, options);
    const json = await response.json();
    // Unwrap the server envelope {ok: true, data: <payload>}.
    // Use hasOwnProperty so we only unwrap real envelopes, not arbitrary objects.
    // Return json.data directly (even when null/[]) — never fall back to the full wrapper
    // object, which would cause "*.map is not a function" when callers expect an array.
    if (json && typeof json === 'object' && Object.prototype.hasOwnProperty.call(json, 'data')) {
        return json.data;   // may be [], null, or an object — caller must guard
    }
    return json;
}

function formBody(params) {
    return {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(params).toString(),
    };
}

/* ============================================================
   UTILITY - DOM helpers
   ============================================================ */
const $ = (sel) => document.querySelector(sel);
const statusBadge = (status) => {
    const map = {
        'Confirmed':    'badge-success',
        'Occupied':     'badge-primary',
        'Available':    'badge-success',
        'Checked-In':   'badge-primary',
        'Checked-Out':  'badge-secondary',
        'CheckedOut':   'badge-secondary',   // legacy compat
        'Cancelled':    'badge-secondary',
        'Pending':      'badge-warning',
    };
    return `<span class="badge ${map[status] || 'badge-secondary'}">${status}</span>`;
};
const currency = (v) => `Rs. ${parseFloat(v || 0).toFixed(2)}`;

/* ============================================================
   APP - Main Controller
   ============================================================ */
const App = {

    currentView: 'dashboard',
    guestsData:  [],
    roomsData:   [],

    // ----------------------------------------------------------
    // Bootstrap
    // ----------------------------------------------------------
    init() {
        // Auth guard
        if (sessionStorage.getItem('loggedIn') !== 'true') {
            window.location.href = 'index.html';
            return;
        }

        // Header date
        document.getElementById('headerDate').textContent =
            new Date().toLocaleDateString('en-US',
                { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

        // Username display
        const username = sessionStorage.getItem('username') || 'Admin';
        const role     = sessionStorage.getItem('role') || 'Staff';
        document.getElementById('sidebarUsername').textContent = username;
        document.getElementById('userAvatar').textContent = username.charAt(0).toUpperCase();
        const uroleEl = document.getElementById('uroleDisplay');
        if (uroleEl) uroleEl.textContent = role;

        // Navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => App.navigate(item.dataset.view));
        });

        // Logout
        document.getElementById('logoutBtn').addEventListener('click', App.logout);

        // Button handlers
        document.getElementById('addGuestBtn').addEventListener('click', GuestModule.openAdd);
        document.getElementById('saveGuestBtn').addEventListener('click', GuestModule.save);
        document.getElementById('newResvBtn').addEventListener('click',   ReservationModule.openNew);
        document.getElementById('saveResvBtn').addEventListener('click',  ReservationModule.save);
        document.getElementById('lookupBillBtn').addEventListener('click', BillingModule.lookup);
        document.getElementById('checkoutBtn').addEventListener('click',   BillingModule.checkout);
        document.getElementById('roomStatusFilter').addEventListener('change', RoomModule.applyFilter);
        document.getElementById('addUserBtn').addEventListener('click',  UsersModule.openAdd);
        document.getElementById('saveUserBtn').addEventListener('click',  UsersModule.save);

        // Load initial view
        App.navigate('dashboard');
    },

    // ----------------------------------------------------------
    // Navigation
    // ----------------------------------------------------------
    navigate(view) {
        App.currentView = view;

        // Update active nav item
        document.querySelectorAll('.nav-item').forEach(i => {
            i.classList.toggle('active', i.dataset.view === view);
        });

        // Show/hide sections
        document.querySelectorAll('.view-section').forEach(s => {
            s.classList.toggle('active', s.id === 'view-' + view);
        });

        // Update page title
        const titles = {
            dashboard:    'Dashboard',
            guests:       'Guest Management',
            rooms:        'Room Management',
            reservations: 'Reservations',
            billing:      'Billing & Checkout',
            users:        'User Management',
        };
        document.getElementById('pageTitle').textContent = titles[view] || view;

        // Load data for the view
        const loaders = {
            dashboard:    DashboardModule.load,
            guests:       GuestModule.load,
            rooms:        RoomModule.load,
            reservations: ReservationModule.load,
            users:        UsersModule.load,
        };
        if (loaders[view]) loaders[view]();
    },

    // ----------------------------------------------------------
    // Modal helpers
    // ----------------------------------------------------------
    openModal(id) { document.getElementById(id).classList.add('open'); },
    closeModal(id) { document.getElementById(id).classList.remove('open'); },

    // ----------------------------------------------------------
    // Logout
    // ----------------------------------------------------------
    async logout() {
        try {
            await apiFetch('logout', { method: 'POST' });
        } catch (_) {}
        sessionStorage.clear();
        window.location.href = 'index.html';
    },
};

/* ============================================================
   DASHBOARD MODULE
   ============================================================ */
const DashboardModule = {
    async load() {
        try {
            const data = await apiFetch('api/dashboard');
            if (data && data.ok === false) {
                Toast.error(data.message || 'Failed to load dashboard.');
                return;
            }
            if (data && typeof data === 'object') {
                document.getElementById('stat-available').textContent = data.availableRooms;
                document.getElementById('stat-guests').textContent    = data.activeGuests;
                document.getElementById('stat-checkins').textContent  = data.todayCheckins;
                document.getElementById('stat-total').textContent     = data.totalRooms;
                // Quick Stats: show occupiedRooms if the dashboard card exists
                const occupiedEl = document.getElementById('stat-occupied');
                if (occupiedEl) occupiedEl.textContent = data.occupiedRooms;
            }

            // Recent reservations (last 5)
            const r = await apiFetch('api/reservations');
            const tbody = document.getElementById('dashRecentBody');
            const reservations = Array.isArray(r) ? r : [];
            if (reservations.length > 0) {
                const recent = reservations.slice(-5).reverse();
                tbody.innerHTML = recent.map(res => `
                    <tr>
                        <td><strong>#${res.reservationNumber}</strong></td>
                        <td>${res.guestId}</td>
                        <td>Room ${res.roomNumber}</td>
                        <td>${res.checkInDate}</td>
                        <td>${res.checkOutDate}</td>
                        <td>${statusBadge(res.status)}</td>
                    </tr>`).join('');
            } else {
                tbody.innerHTML = `<tr><td colspan="6">
                    <div class="empty-state"><div class="empty-icon">\uD83D\uDCCB</div><p>No reservations yet.</p></div>
                </td></tr>`;
            }
        } catch (e) {
            Toast.error('Failed to load dashboard: ' + e.message);
        }
    }
};

/* ============================================================
   GUEST MODULE
   ============================================================ */
const GuestModule = {
    async load() {
        const tbody = document.getElementById('guestsTableBody');
        tbody.innerHTML = `<tr><td colspan="5"><div class="loading-wrap"><div class="spinner"></div></div></td></tr>`;
        try {
            const guests = await apiFetch('api/guests');
            App.guestsData = Array.isArray(guests) ? guests : [];
            GuestModule.render(App.guestsData);
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="5" style="color:var(--danger);padding:20px">Error: ${e.message}</td></tr>`;
        }
    },

    render(guests) {
        const tbody = document.getElementById('guestsTableBody');
        if (!Array.isArray(guests) || guests.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5">
                <div class="empty-state"><div class="empty-icon">\uD83D\uDC65</div><p>No guests registered yet.</p></div>
            </td></tr>`;
            return;
        }
        tbody.innerHTML = guests.map(g => `
            <tr>
                <td><strong>${g.guestId}</strong></td>
                <td>${escHtml(g.name)}</td>
                <td>${escHtml(g.address)}</td>
                <td>${escHtml(g.contactNumber)}</td>
                <td>
                    <button class="btn btn-warning btn-sm" onclick="GuestModule.openEdit(${g.guestId})">Edit</button>
                    <button class="btn btn-danger btn-sm" style="margin-left:6px" onclick="GuestModule.deleteGuest(${g.guestId}, '${escHtml(g.name)}')">Delete</button>
                </td>
            </tr>`).join('');
    },

    openAdd() {
        document.getElementById('guestModalTitle').textContent = 'Add Guest';
        document.getElementById('guestId').value      = '';
        document.getElementById('guestName').value    = '';
        document.getElementById('guestAddress').value = '';
        document.getElementById('guestContact').value = '';
        App.openModal('guestModal');
    },

    openEdit(id) {
        const g = App.guestsData.find(x => x.guestId === id);
        if (!g) { Toast.error('Guest not found.'); return; }
        document.getElementById('guestModalTitle').textContent = 'Edit Guest';
        document.getElementById('guestId').value      = g.guestId;
        document.getElementById('guestName').value    = g.name;
        document.getElementById('guestAddress').value = g.address;
        document.getElementById('guestContact').value = g.contactNumber;
        App.openModal('guestModal');
    },

    async save() {
        const id      = document.getElementById('guestId').value.trim();
        const name    = document.getElementById('guestName').value.trim();
        const address = document.getElementById('guestAddress').value.trim();
        const contact = document.getElementById('guestContact').value.trim();

        if (!name || !address || !contact) {
            Toast.error('All fields are required.'); return;
        }

        try {
            if (id) {
                // PUT update — raw fetch; body is the new envelope { ok, data, error }
                const params = new URLSearchParams({ guest_id: id, name, address, contact_number: contact });
                const res  = await fetch(BASE + 'api/guests?' + params.toString(), { method: 'PUT' });
                const body = await res.json();
                if (body.ok) {
                    Toast.success('Guest updated.');
                    App.closeModal('guestModal');
                    GuestModule.load();
                } else {
                    Toast.error(body.message || 'Failed to save guest.');
                }
            } else {
                const r = await apiFetch('api/guests', formBody({ name, address, contact_number: contact }));
                if (r && r.ok === false) {
                    Toast.error(r.message || 'Failed to save guest.');
                } else if (r && r.success) {
                    Toast.success('Guest added successfully.');
                    App.closeModal('guestModal');
                    GuestModule.load();
                } else {
                    Toast.error('Failed to save guest.');
                }
            }
        } catch (e) {
            Toast.error('Error: ' + e.message);
        }
    },

    async deleteGuest(id, name) {
        if (!confirm(`Delete guest "${name}" (ID: ${id})? This cannot be undone.`)) return;
        try {
            const res  = await fetch(`${BASE}api/guests?id=${id}`, { method: 'DELETE' });
            const body = await res.json(); // envelope: { ok, data, error }
            if (body.ok) {
                Toast.success('Guest deleted.');
                GuestModule.load();
            } else {
                Toast.error(body.message || 'Failed to delete guest.');
            }
        } catch (e) {
            Toast.error('Error: ' + e.message);
        }
    },
};

/* ============================================================
   ROOM MODULE
   ============================================================ */
const RoomModule = {
    async load() {
        const tbody = document.getElementById('roomsTableBody');
        tbody.innerHTML = `<tr><td colspan="4"><div class="loading-wrap"><div class="spinner"></div></div></td></tr>`;
        try {
            const rooms = await apiFetch('api/rooms');
            App.roomsData = Array.isArray(rooms) ? rooms : [];
            RoomModule.render(App.roomsData);
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="4" style="color:var(--danger);padding:20px">Error: ${e.message}</td></tr>`;
        }
    },

    applyFilter() {
        const filter = document.getElementById('roomStatusFilter').value;
        const baseRooms = Array.isArray(App.roomsData) ? App.roomsData : [];
        const filtered = filter === 'all'
            ? baseRooms
            : baseRooms.filter(r => r.status === filter);
        RoomModule.render(filtered);
    },

    render(rooms) {
        const tbody = document.getElementById('roomsTableBody');
        if (!Array.isArray(rooms) || rooms.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4">
                <div class="empty-state"><div class="empty-icon">\uD83C\uDFE0</div><p>No rooms found.</p></div>
            </td></tr>`;
            return;
        }
        tbody.innerHTML = rooms.map(r => `
            <tr>
                <td><strong>${r.roomNumber}</strong></td>
                <td>${escHtml(r.roomType)}</td>
                <td>${currency(r.pricePerNight)}</td>
                <td>${statusBadge(r.status)}</td>
            </tr>`).join('');
    },
};

/* ============================================================
   RESERVATION MODULE
   ============================================================ */
const ReservationModule = {
    async load() {
        const tbody = document.getElementById('resvTableBody');
        tbody.innerHTML = `<tr><td colspan="8"><div class="loading-wrap"><div class="spinner"></div></div></td></tr>`;
        try {
            const data = await apiFetch('api/reservations');
            const reservations = Array.isArray(data) ? data : [];
            if (reservations.length > 0) {
                const ordered = [...reservations].reverse();
                tbody.innerHTML = ordered.map(r => `
                    <tr>
                        <td><strong>#${r.reservationNumber}</strong></td>
                        <td>${r.guestId}</td>
                        <td>Room ${r.roomNumber}</td>
                        <td>${r.checkInDate}</td>
                        <td>${r.checkOutDate}</td>
                        <td>${currency(r.totalBill)}</td>
                        <td>${statusBadge(r.status)}</td>
                        <td>
                            <button class="btn btn-success btn-sm"
                                onclick="BillingModule.quickCheckout(${r.reservationNumber})"
                                ${r.status === 'Checked-Out' ? 'disabled' : ''}>
                                Checkout
                            </button>
                        </td>
                    </tr>`).join('');
            } else {
                tbody.innerHTML = `<tr><td colspan="8">
                    <div class="empty-state"><div class="empty-icon">\uD83D\uDCCB</div><p>No reservations found.</p></div>
                </td></tr>`;
            }
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="8" style="color:var(--danger);padding:20px">Error: ${e.message}</td></tr>`;
        }
    },

    async openNew() {
        // Pre-populate guest and room dropdowns
        const guestSel = document.getElementById('resvGuestId');
        const roomSel  = document.getElementById('resvRoomNum');
        const errEl    = document.getElementById('resvError');

        errEl.className = 'alert'; errEl.textContent = '';
        guestSel.innerHTML = '<option value="">Loading...</option>';
        roomSel.innerHTML  = '<option value="">Loading...</option>';

        // Set default dates
        const today = new Date();
        const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
        document.getElementById('resvCheckIn').value  = today.toISOString().slice(0, 10);
        document.getElementById('resvCheckOut').value = tomorrow.toISOString().slice(0, 10);

        App.openModal('resvModal');

        try {
            const [gRes, rRes] = await Promise.all([
                apiFetch('api/guests'),
                apiFetch('api/rooms?available=true'),
            ]);

            const guests = Array.isArray(gRes) ? gRes : [];
            const rooms = Array.isArray(rRes) ? rRes : [];

            guestSel.innerHTML = guests.length
                ? guests.map(g =>
                    `<option value="${g.guestId}">ID ${g.guestId} \u2014 ${escHtml(g.name)}</option>`
                  ).join('')
                : '<option value="">No guests registered</option>';

            roomSel.innerHTML = rooms.length
                ? rooms.map(r =>
                    `<option value="${r.roomNumber}">Room ${r.roomNumber} \u2014 ${escHtml(r.roomType)} (${currency(r.pricePerNight)}/night)</option>`
                  ).join('')
                : '<option value="">No available rooms</option>';
        } catch (e) {
            guestSel.innerHTML = '<option value="">Failed to load guests</option>';
            roomSel.innerHTML  = '<option value="">Failed to load rooms</option>';
            Toast.error('Failed to load options: ' + e.message);
        }
    },

    async save() {
        const guestId  = document.getElementById('resvGuestId').value;
        const roomNum  = document.getElementById('resvRoomNum').value;
        const checkIn  = document.getElementById('resvCheckIn').value;
        const checkOut = document.getElementById('resvCheckOut').value;
        const errEl    = document.getElementById('resvError');

        errEl.className = 'alert'; errEl.textContent = '';

        if (!guestId || !roomNum || !checkIn || !checkOut) {
            errEl.className = 'alert alert-danger show';
            errEl.textContent = 'All fields are required.';
            return;
        }
        if (checkOut <= checkIn) {
            errEl.className = 'alert alert-danger show';
            errEl.textContent = 'Check-out date must be after check-in date.';
            return;
        }

        try {
            document.getElementById('saveResvBtn').disabled = true;
            const data = await apiFetch('api/reservations', formBody({
                guest_id: guestId,
                room_number: roomNum,
                check_in_date: checkIn,
                check_out_date: checkOut,
            }));

            if (data && data.ok === false) {
                errEl.className = 'alert alert-danger show';
                errEl.textContent = data.message || 'Failed to create reservation.';
            } else if (data && data.success) {
                Toast.success(`Reservation #${data.reservation_id} created!`);
                App.closeModal('resvModal');
                ReservationModule.load();
            } else {
                errEl.className = 'alert alert-danger show';
                errEl.textContent = data?.message || 'Failed to create reservation.';
            }
        } catch (e) {
            errEl.className = 'alert alert-danger show';
            errEl.textContent = 'Server error: ' + e.message;
        } finally {
            document.getElementById('saveResvBtn').disabled = false;
        }
    },
};

/* ============================================================
   BILLING MODULE
   ============================================================ */
const BillingModule = {
    currentResvId: null,

    // Called from the Billing view
    async lookup() {
        const id = document.getElementById('billResvId').value.trim();
        if (!id) { Toast.error('Please enter a reservation number.'); return; }
        await BillingModule.fetchAndShow(parseInt(id));
    },

    // Called from Reservations table quick-checkout button
    async quickCheckout(resvId) {
        App.navigate('billing');
        document.getElementById('billResvId').value = resvId;
        // Small delay so the view transition completes
        setTimeout(() => BillingModule.fetchAndShow(resvId), 100);
    },

    async fetchAndShow(id) {
        const panel = document.getElementById('receiptPanel');
        panel.style.display = 'none';

        try {
            const data = await apiFetch(`api/billing?id=${id}`);
            if (data && data.ok === false) {
                Toast.error(data.message || 'Reservation not found.');
                return;
            }
            if (!data || typeof data !== 'object') {
                Toast.error('Invalid billing response.');
                return;
            }

            BillingModule.currentResvId = data.reservation_id;

            document.getElementById('r-id').textContent      = data.reservation_id;
            document.getElementById('r-guest').textContent   = data.guest_id;
            document.getElementById('r-room').textContent    = 'Room ' + data.room_number;
            document.getElementById('r-checkin').textContent = data.check_in_date;
            document.getElementById('r-checkout').textContent = data.check_out_date;
            document.getElementById('r-total').textContent   = currency(data.total_bill);

            const statusEl = document.getElementById('receiptStatus');
            const isCheckedOut = data.status === 'Checked-Out' || data.status === 'CheckedOut';
            statusEl.className = 'badge ' + (isCheckedOut ? 'badge-secondary' : 'badge-primary');
            statusEl.textContent = data.status;

            const checkoutBtn = document.getElementById('checkoutBtn');
            checkoutBtn.disabled = isCheckedOut;
            checkoutBtn.textContent = isCheckedOut
                ? '\u2713 Already Checked Out'
                : '\u2713 Process Checkout & Calculate Bill';

            panel.style.display = '';
        } catch (e) {
            Toast.error('Error: ' + e.message);
        }
    },

    async checkout() {
        if (!BillingModule.currentResvId) return;
        if (!confirm(`Process checkout for Reservation #${BillingModule.currentResvId}? This will run the billing procedure.`)) return;

        try {
            document.getElementById('checkoutBtn').disabled = true;
            const data = await apiFetch('api/billing/checkout', formBody({
                reservation_id: BillingModule.currentResvId,
            }));

            if (data && data.ok === false) {
                Toast.error(data.message || 'Checkout failed.');
                document.getElementById('checkoutBtn').disabled = false;
            } else if (data?.success) {
                Toast.success(`Checkout complete! Total: ${currency(data.total_bill)}`);
                // Refresh the receipt panel with the final bill
                await BillingModule.fetchAndShow(BillingModule.currentResvId);
                // UPGRADE: Immediately sync the Room Management view — the room is now Available
                RoomModule.load();
            } else {
                Toast.error('Checkout failed.');
                document.getElementById('checkoutBtn').disabled = false;
            }
        } catch (e) {
            Toast.error('Error: ' + e.message);
            document.getElementById('checkoutBtn').disabled = false;
        }
    },
};

/* ============================================================
   USERS MODULE
   ============================================================ */
const UsersModule = {
    usersData: [],

    async load() {
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = `<tr><td colspan="4"><div class="loading-wrap"><div class="spinner"></div></div></td></tr>`;
        try {
            const users = await apiFetch('api/users');
            UsersModule.usersData = Array.isArray(users) ? users : [];
            UsersModule.render(UsersModule.usersData);
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="4" style="color:var(--danger);padding:20px">Error: ${e.message}</td></tr>`;
        }
    },

    render(users) {
        const tbody = document.getElementById('usersTableBody');
        if (!Array.isArray(users) || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4">
                <div class="empty-state"><div class="empty-icon">\uD83D\uDC64</div><p>No users found.</p></div>
            </td></tr>`;
            return;
        }
        tbody.innerHTML = users.map(u => `
            <tr>
                <td><strong>${u.userId}</strong></td>
                <td>${escHtml(u.username)}</td>
                <td><span class="badge ${u.role === 'Admin' ? 'badge-primary' : 'badge-secondary'}">${escHtml(u.role)}</span></td>
                <td>
                    <button class="btn btn-warning btn-sm" onclick="UsersModule.openEdit(${u.userId})">Edit</button>
                    <button class="btn btn-danger btn-sm" style="margin-left:6px" onclick="UsersModule.deleteUser(${u.userId}, '${escHtml(u.username)}')">Delete</button>
                </td>
            </tr>`).join('');
    },

    openAdd() {
        document.getElementById('userModalTitle').textContent = 'Add User';
        document.getElementById('userId').value       = '';
        document.getElementById('userUsername').value = '';
        document.getElementById('userPassword').value = '';
        document.getElementById('userRole').value     = 'Staff';
        App.openModal('userModal');
    },

    openEdit(id) {
        const u = UsersModule.usersData.find(x => x.userId === id);
        if (!u) { Toast.error('User not found.'); return; }
        document.getElementById('userModalTitle').textContent = 'Edit User';
        document.getElementById('userId').value       = u.userId;
        document.getElementById('userUsername').value = u.username;
        document.getElementById('userPassword').value = '';   // never pre-fill password
        document.getElementById('userRole').value     = u.role || 'Staff';
        App.openModal('userModal');
    },

    async save() {
        const id       = document.getElementById('userId').value.trim();
        const username = document.getElementById('userUsername').value.trim();
        const password = document.getElementById('userPassword').value.trim();
        const role     = document.getElementById('userRole').value;

        if (!username) { Toast.error('Username is required.'); return; }
        if (!id && !password) { Toast.error('Password is required for new users.'); return; }

        try {
            if (id) {
                // PUT update — raw fetch; body is the new envelope { ok, data, error }
                const params = new URLSearchParams({ user_id: id, username, password, role });
                const res  = await fetch(BASE + 'api/users?' + params.toString(), { method: 'PUT' });
                const body = await res.json();
                if (body.ok) {
                    Toast.success('User updated.');
                    App.closeModal('userModal');
                    UsersModule.load();
                } else {
                    Toast.error(body.message || 'Failed to save user.');
                }
            } else {
                const r = await apiFetch('api/users', formBody({ username, password, role }));
                if (r && r.ok === false) {
                    Toast.error(r.message || 'Failed to save user.');
                } else if (r && r.success) {
                    Toast.success('User created successfully.');
                    App.closeModal('userModal');
                    UsersModule.load();
                } else {
                    Toast.error('Failed to save user.');
                }
            }
        } catch (e) {
            Toast.error('Error: ' + e.message);
        }
    },

    async deleteUser(id, name) {
        if (!confirm(`Delete user "${name}" (ID: ${id})? This cannot be undone.`)) return;
        try {
            const res  = await fetch(`${BASE}api/users?id=${id}`, { method: 'DELETE' });
            const body = await res.json(); // envelope: { ok, data, error }
            if (body.ok) {
                Toast.success('User deleted.');
                UsersModule.load();
            } else {
                Toast.error(body.message || 'Failed to delete user.');
            }
        } catch (e) {
            Toast.error('Error: ' + e.message);
        }
    },
};

/* ============================================================
   UTILITIES
   ============================================================ */
function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Close modal on overlay click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.classList.remove('open');
    });
});

/* ============================================================
   ENTRYPOINT
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => App.init());
