const API_BASE = '/api';
let accessKey = null;
let customers = [];

// Initialize Telegram WebApp
const tg = window.Telegram.WebApp;
tg.expand();

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await authenticate();
        showApp();
        loadCustomers();
    } catch (e) {
        console.error("Auth failed", e);
        document.getElementById('loading').classList.add('hidden');

        // Show manual login form
        document.getElementById('manual-login').classList.remove('hidden');
        document.getElementById('manual-chat-id').focus();
    }
});

async function authenticate() {
    // 1. Try Telegram WebApp 
    const user = tg.initDataUnsafe?.user;
    if (user) {
        return await doLogin(user.id);
    }

    // 2. Try URL Query Parameter (Fallback for Ngrok/Testing)
    const urlParams = new URLSearchParams(window.location.search);
    const chatId = urlParams.get('chat_id');
    if (chatId) {
        console.log("Using URL chatId fallback:", chatId);
        return await doLogin(chatId);
    }

    throw new Error("Login failed");
}

async function handleManualLogin(e) {
    e.preventDefault();
    const chatId = document.getElementById('manual-chat-id').value;
    if (!chatId) return;

    try {
        await doLogin(chatId);
        document.getElementById('manual-login').classList.add('hidden');
        showApp();
        loadCustomers();
    } catch (err) {
        alert("Login xatosi: " + err.message);
    }
}

async function doLogin(chatId) {
    const res = await fetch(`${API_BASE}/auth/telegram-login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ chatId: chatId })
    });

    if (!res.ok) throw new Error("Login failed");
    const data = await res.json();
    accessKey = data.accessKey;
}

function showApp() {
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');
}

// Data Loading
async function loadCustomers() {
    const res = await fetch(`${API_BASE}/customers`, {
        headers: { 'X-ACCESS-KEY': accessKey }
    });
    const data = await res.json();
    console.log("Customers data: ", data); // Debug log
    try {
        const res = await fetch(`${API_BASE}/customers`, {
            headers: { 'X-ACCESS-KEY': accessKey }
        });
        if (res.status === 401) {
            handleAuthError();
            return;
        }
        customers = await res.json();
        renderCustomers(customers);
        document.getElementById('customer-count-badge').innerText = customers.length;
    } catch (e) {
        console.error(e);
    }
}

function renderCustomers(list) {
    const container = document.getElementById('customer-list-container');
    container.innerHTML = '';

    // Sort by checking due dates but mainly just list.
    // For visual match, we render cards.

    // Let's create a fake date grouping for visual demo based on index if real date is missing
    // Or just simple list.

    list.forEach(c => {
        const div = document.createElement('div');
        div.className = 'customer-card';
        div.onclick = () => showCustomerDetails(c.id);

        const balance = c.balance || 0;
        const isNegative = balance < 0;
        const balanceText = balance.toLocaleString() + " UZS";

        div.innerHTML = `
            <div class="card-left">
                <div class="avatar">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                </div>
                <div class="info">
                    <h4>${c.fullName}</h4>
                    <p>${c.phone || ''}</p>
                </div>
            </div>
            <div class="balance ${isNegative ? 'negative' : 'positive'}">
                ${balanceText}
            </div>
         `;
        container.appendChild(div);
    });
}

function filterCustomers() {
    const query = document.getElementById('search-input').value.toLowerCase();
    const filtered = customers.filter(c =>
        c.fullName.toLowerCase().includes(query) ||
        (c.phone && c.phone.includes(query))
    );
    renderCustomers(filtered);
}


// Customer Details Logic
let currentViewedCustomerId = null;

async function showCustomerDetails(id) {
    currentViewedCustomerId = id;
    document.getElementById('loading').classList.remove('hidden');
    try {
        const res = await fetch(`${API_BASE}/customers/${id}/transactions`, {
            headers: { 'X-ACCESS-KEY': accessKey }
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(`Server Xatosi (${res.status}): ${errText}`);
        }

        const transactions = await res.json();

        const customer = customers.find(c => c.id === id);
        renderCustomerDetails(customer, transactions);

        // Show details tab
        document.getElementById('home-view').classList.add('hidden');
        document.getElementById('customer-details-view').classList.remove('hidden');
        tg.BackButton.show();
    } catch (e) {
        console.error(e);
        tg.showAlert("Xatolik: " + e.message);
    } finally {
        document.getElementById('loading').classList.add('hidden');
    }
}

function renderCustomerDetails(customer, transactions) {
    document.getElementById('details-name').innerText = customer.fullName;
    document.getElementById('details-phone').innerText = customer.phone;

    let totalDebt = 0;
    let totalPaid = 0;

    const list = document.getElementById('details-debt-list');
    list.innerHTML = '';

    if (transactions.length === 0) {
        list.innerHTML = '<p style="text-align:center; padding:20px; color:#999;">Tarix bo\'sh</p>';
    } else {
        transactions.forEach(t => {
            // Calc totals
            if (t.type === 'DEBT') {
                totalDebt += t.amount;
            } else {
                totalPaid += t.amount;
            }

            const item = document.createElement('div');
            item.className = 'debt-item';

            const isDebt = t.type === 'DEBT';
            const color = isDebt ? '#ff3b30' : '#4cd964'; // Red vs Green
            const sign = isDebt ? '' : '-'; // Optional

            item.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px;">
                    <span style="font-weight: 500; font-size: 16px; color: ${color}">${sign}${t.amount.toLocaleString()} UZS</span>
                    <span style="font-size: 12px; color: #999;">${t.date}</span>
                </div>
                <div style="font-size: 13px; color: #888;">${t.description || (isDebt ? 'Qarz' : "To'lov")}</div>
            `;
            list.appendChild(item);
        });
    }

    // Update Summary
    // Note: totalDebt - totalPaid might differ from customer.balance if transaction history is partial.
    // For now we assume full history.
    document.getElementById('summary-debt').innerText = totalDebt.toLocaleString() + " UZS";
    document.getElementById('summary-pay').innerText = totalPaid.toLocaleString() + " UZS";

    // Calculate balance
    // Positive balance = Owed (Debt > Paid) -> Red
    // Negative balance = Surplus (Paid > Debt) -> Green ?? 
    // Usually Balance = Debt - Paid.
    const balance = totalDebt - totalPaid; // Or use customer.balance
    const balanceText = balance.toLocaleString() + " UZS";
    const balanceEl = document.getElementById('summary-balance');
    balanceEl.innerText = balanceText;

    if (balance > 0) {
        balanceEl.style.color = '#dc3545'; // Red (Qarzdor)
    } else if (balance < 0) {
        balanceEl.style.color = '#28a745'; // Green (Haqdor)
    } else {
        balanceEl.style.color = '#333';
    }
}

// Telegram Back Button
tg.BackButton.onClick(() => {
    document.getElementById('customer-details-view').classList.add('hidden');
    document.getElementById('home-view').classList.remove('hidden');
    tg.BackButton.hide();
});

function openAddDebtFromDetails() {
    if (!currentViewedCustomerId) return;
    const customer = customers.find(c => c.id === currentViewedCustomerId);
    if (!customer) return;

    document.getElementById('add-debt-modal').classList.remove('hidden');
    document.getElementById('debt-customer-id').value = customer.id;
    document.getElementById('debt-customer-name').value = customer.fullName;
    document.getElementById('debt-amount').value = '';
    document.getElementById('debt-due-date').value = '';
    document.getElementById('debt-desc').value = '';
    document.getElementById('debt-amount').focus();
}

function updateDebtSelect() {
    // This function was used for the old Add Debt Tab dropdown.
    // Since we might not have that tab, this is less critical, but good to keep safe.
    const select = document.getElementById('debt-customer-select');
    if (!select) return;

    select.innerHTML = '<option value="" disabled selected>Tanlang...</option>';
    customers.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = c.fullName;
        select.appendChild(opt);
    });
}

// --- Navigation & Stats ---

window.switchView = async (viewId) => {
    // Hide all main content views
    ['home-view', 'stats-view', 'profile-view', 'customer-details-view'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.add('hidden');
    });

    // Show requested view
    const target = document.getElementById(viewId + '-view');
    if (target) target.classList.remove('hidden');

    tg.BackButton.hide();

    // Update Bottom Nav
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    // Find active nav based on onclick
    const activeNav = document.querySelector(`.nav-item[onclick="switchView('${viewId}')"]`);
    if (activeNav) activeNav.classList.add('active');

    if (viewId === 'home') {
        loadCustomers();
    } else if (viewId === 'stats') {
        loadStatistics();
    }
};

async function loadStatistics() {
    try {
        const resStats = await fetch(`${API_BASE}/shop/stats`, {
            headers: { 'X-ACCESS-KEY': accessKey }
        });
        const stats = await resStats.json();

        const totalDebt = stats.totalDebt || 0;
        const totalPaid = stats.totalPaid || 0;
        const balance = totalDebt - totalPaid; // Net Debt

        // Chiqim (Total Debt) - displayed as negative
        document.getElementById('stat-total-debt').innerText = "-" + totalDebt.toLocaleString() + " UZS";
        // Kirim (Total Paid) - displayed as positive
        document.getElementById('stat-total-paid').innerText = totalPaid.toLocaleString() + " UZS";

        // Balance Bars Logic
        // If balance > 0 (Debt > Paid), we have Chiqim Balansi (Outstanding Debt).
        // If balance < 0 (Paid > Debt), we have Kirim Balansi (Overpaid/Surplus).

        let chiqimBalansi = 0;
        let kirimBalansi = 0;

        if (balance > 0) {
            chiqimBalansi = balance;
        } else {
            kirimBalansi = Math.abs(balance);
        }

        document.getElementById('bar-debt-val').innerText = "-" + chiqimBalansi.toLocaleString() + " UZS";
        document.getElementById('bar-pay-val').innerText = kirimBalansi.toLocaleString() + " UZS";

        // Load Transactions List
        const resTrans = await fetch(`${API_BASE}/shop/transactions`, {
            headers: { 'X-ACCESS-KEY': accessKey }
        });
        const transactions = await resTrans.json();
        renderStatsTransactions(transactions);

    } catch (e) {
        console.error("Stats error", e);
    }
}

function renderStatsTransactions(transactions) {
    const container = document.getElementById('stats-transactions-list');
    container.innerHTML = '';

    if (transactions.length === 0) {
        container.innerHTML = '<p style="text-align:center; padding:20px; color:#999;">Tranzaksiyalar yo\'q</p>';
        return;
    }

    transactions.forEach(t => {
        const div = document.createElement('div');
        div.className = 'customer-card'; // Reuse styled card
        div.style.borderRadius = '0';
        div.style.boxShadow = 'none';
        div.style.borderBottom = '1px solid #eee';
        div.style.marginBottom = '0';

        const isDebt = t.type === 'DEBT';
        const color = isDebt ? '#ff3b30' : '#4cd964';
        const sign = isDebt ? '-' : '+';

        div.innerHTML = `
            <div class="card-left">
                <div class="avatar" style="width:40px; height:40px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                </div>
                <div class="info">
                    <h4 style="font-size:15px;">${t.description}</h4>
                    <p>${t.date}</p>
                </div>
            </div>
            <div class="balance" style="color:${color}">
                ${sign}${t.amount.toLocaleString()} UZS
            </div>
        `;
        container.appendChild(div);
    });
}

// --- Modals & Forms ---

window.showAddCustomerModal = () => {
    document.getElementById('add-customer-modal').classList.remove('hidden');
};

window.closeAddCustomerModal = () => {
    document.getElementById('add-customer-modal').classList.add('hidden');
    document.getElementById('add-customer-form').reset();
};

window.openPaymentModalFromDetails = () => {
    if (!currentViewedCustomerId) return;
    document.getElementById('payment-modal').classList.remove('hidden');
    document.getElementById('payment-amount').value = '';
    document.getElementById('payment-amount').focus();
};

window.closePaymentModal = () => {
    document.getElementById('payment-modal').classList.add('hidden');
};

window.fillFullPayment = () => {
    if (!currentViewedCustomerId) return;
    const customer = customers.find(c => c.id === currentViewedCustomerId);
    if (customer && customer.balance > 0) {
        document.getElementById('payment-amount').value = customer.balance;
    }
};

// Event Listeners (Global)
// We need to attach these once to avoid duplication if we reload scripts (unlikely but safe)
// Using Named functions or checking if already attached is hard in vanilla without flags.
// But since this script runs once on load, it's fine.

document.getElementById('payment-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const amount = document.getElementById('payment-amount').value;

    try {
        const res = await fetch(`${API_BASE}/payments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-ACCESS-KEY': accessKey
            },
            body: JSON.stringify({
                customerId: currentViewedCustomerId,
                amount: amount
            })
        });

        if (res.ok) {
            tg.showAlert("To'lov qabul qilindi!");
            closePaymentModal();
            // Refresh data
            await loadCustomers();
            if (currentViewedCustomerId) showCustomerDetails(currentViewedCustomerId);
        } else {
            const err = await res.json();
            tg.showAlert("Xatolik: " + (err.message || "To'lov amalga oshmadi"));
        }
    } catch (err) {
        tg.showAlert("Internet xatosi");
    }
});

document.getElementById('add-customer-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('new-customer-name').value;
    const phone = document.getElementById('new-customer-phone').value;

    try {
        const res = await fetch(`${API_BASE}/customers`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-ACCESS-KEY': accessKey
            },
            body: JSON.stringify({ fullName: name, phone: phone })
        });

        if (res.ok) {
            tg.showAlert("Mijoz qo'shildi!");
            closeAddCustomerModal();
            loadCustomers();
        } else {
            tg.showAlert("Xatolik yuz berdi");
        }
    } catch (err) {
        tg.showAlert("Internet xatosi");
    }
});

// Add Debt Form Listener
const addDebtForm = document.getElementById('add-debt-form');
if (addDebtForm) {
    addDebtForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        // Use hidden ID input instead of select
        const customerId = document.getElementById('debt-customer-id').value;
        const amount = document.getElementById('debt-amount').value;
        const dueDate = document.getElementById('debt-due-date').value;
        const desc = document.getElementById('debt-desc').value;

        try {
            const res = await fetch(`${API_BASE}/debts`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-ACCESS-KEY': accessKey
                },
                body: JSON.stringify({
                    customer: { id: customerId },
                    totalAmount: amount,
                    dueDate: dueDate,
                    description: desc
                })
            });

            if (res.ok) {
                tg.showAlert("Qarz saqlandi!");
                document.getElementById('add-debt-modal').classList.add('hidden');
                addDebtForm.reset();

                // Refresh data
                await loadCustomers();

                // If we are in details view, refresh it
                if (currentViewedCustomerId && currentViewedCustomerId == customerId) {
                    showCustomerDetails(currentViewedCustomerId);
                }
            } else {
                tg.showAlert("Xatolik yuz berdi");
            }
        } catch (err) {
            tg.showAlert("Internet xatosi");
        }
    });
}

async function sendDebtReminder() {
    if (!currentViewedCustomerId) return;
    
    // Confirm dialogue? Maybe not needed for quick action, but good practice.
    // Let's just send it and show alert.
    
    if(!confirm("Mijozga SMS (Telegram xabari) yuborilsinmi?")) return;

    try {
        const res = await fetch(`${API_BASE}/customers/${currentViewedCustomerId}/reminder`, {
            method: 'POST',
            headers: { 'X-ACCESS-KEY': accessKey }
        });

        if (res.ok) {
            tg.showAlert("Xabar yuborildi!");
        } else {
            const err = await res.text();
            tg.showAlert("Xatolik: " + (err || "Yuborib bo'lmadi"));
        }
    } catch (e) {
        tg.showAlert("Internet xatosi");
    }
}
