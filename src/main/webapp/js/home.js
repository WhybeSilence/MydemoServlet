const API_BASE = '/servletLogin';

let currentUser = null;

async function apiFetch(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();

    if (data.code === 401) {
        currentUser = null;
        if (typeof updateUserPanel === 'function') {
            updateUserPanel(null);
        }
        const loginModal = document.getElementById('loginModal');
        if (loginModal) {
            loginModal.style.display = 'flex';
            alert('登录已过期，请重新登录');
        } else {
            alert(data.msg || '请先登录');
        }
        return null;
    }

    return data;
}

document.addEventListener('DOMContentLoaded', () => {

    const bgImages = [
        "https://picsum.photos/id/1015/1920/1080",
        "https://picsum.photos/id/1016/1920/1080",
        "https://picsum.photos/id/1018/1920/1080",
        "https://picsum.photos/id/1019/1920/1080"
    ];
    const randomBg = bgImages[Math.floor(Math.random() * bgImages.length)];
    document.body.style.backgroundImage = `url(${randomBg})`;

    const modeToggle = document.getElementById('modeToggle');
    if(modeToggle){
        modeToggle.addEventListener('change', () => {
            document.body.classList.toggle('dark');
        });
    }

    const userPanel = document.getElementById('userPanel');
    if(userPanel){
        document.addEventListener('mousemove', (e) => {
            const winW = window.innerWidth;
            if (winW - e.clientX < 25 || userPanel.matches(':hover')) {
                userPanel.classList.add('show');
            } else {
                userPanel.classList.remove('show');
            }
        });
    }

    const loginBtn = document.querySelector('.login-btn');
    const modalMask = document.getElementById('loginModal');
    const modalBox = document.querySelector('.modal-box');
    const tabItems = document.querySelectorAll('.tab-item');
    const loginForm = document.querySelector('.login-form');
    const registerForm = document.querySelector('.register-form');

    if(loginBtn && modalMask){
        loginBtn.addEventListener('click', () => {
            if (currentUser) {
                return;
            }
            modalMask.style.display = 'flex';
        });

        modalMask.addEventListener('click', (e) => {
            if (!modalBox.contains(e.target)) {
                modalMask.style.display = 'none';
            }
        });
    }

    if(tabItems.length > 0){
        tabItems.forEach(tab => {
            tab.addEventListener('click', () => {
                tabItems.forEach(item => item.classList.remove('active'));
                tab.classList.add('active');
                const type = tab.dataset.tab;
                if (type === 'login') {
                    loginForm.style.display = 'block';
                    registerForm.style.display = 'none';
                } else {
                    loginForm.style.display = 'none';
                    registerForm.style.display = 'block';
                }
            });
        });
    }

    document.querySelectorAll('.sidebar-card a').forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            if (targetId && targetId.startsWith('#')) {
                const targetElement = document.querySelector(targetId);
                if (targetElement) {
                    e.preventDefault();
                    targetElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }
        });
    });

    const loginSubmitBtn = document.getElementById('loginSubmitBtn');
    const registerSubmitBtn = document.getElementById('registerSubmitBtn');

    if(loginSubmitBtn){
        loginSubmitBtn.addEventListener('click', async () => {
            const usernameInput = document.getElementById('loginUsername');
            const passwordInput = document.getElementById('loginPassword');

            const username = usernameInput.value.trim();
            const password = passwordInput.value.trim();

            if (!username || !password) {
                alert('请输入账号和密码！');
                return;
            }

            try {
                const response = await fetch(`${API_BASE}/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert('登录成功！');
                    modalMask.style.display = 'none';
                    loadCurrentUser();
                    loadShopList();
                } else {
                    alert(data.msg || '登录失败');
                }

            } catch (error) {
                console.error('登录错误详情:', error);
                alert('网络异常或服务器响应格式错误');
            }
        });
    }

    if(registerSubmitBtn){
        registerSubmitBtn.addEventListener('click', async () => {
            const uName = document.getElementById('regUsername');
            const uPwd = document.getElementById('regPassword');
            const uConfirm = document.getElementById('regConfirmPwd');

            const username = uName.value.trim();
            const password = uPwd.value.trim();
            const confirmPwd = uConfirm.value.trim();

            if (!username || !password || !confirmPwd) {
                alert('请填写完整的注册信息！');
                return;
            }

            if (password !== confirmPwd) {
                alert('两次输入的密码不一致！');
                return;
            }

            try {
                const response = await fetch(`${API_BASE}/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert('注册成功！请登录');
                    document.querySelector('[data-tab="login"]').click();
                    uName.value = ''; uPwd.value = ''; uConfirm.value = '';
                } else {
                    alert(data.msg || '注册失败');
                }

            } catch (error) {
                console.error('注册错误详情:', error);
                alert('网络异常或服务器响应格式错误');
            }
        });
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            if (!confirm('确定要退出登录吗？')) return;
            try {
                await fetch(`${API_BASE}/logout`, { method: 'POST' });
            } catch (e) {}
            currentUser = null;
            updateUserPanel(null);
            location.reload();
        });
    }

    document.querySelectorAll('.require-login').forEach(link => {
        link.addEventListener('click', function(e) {
            if (!currentUser) {
                e.preventDefault();
                const loginModal = document.getElementById('loginModal');
                if (loginModal) {
                    loginModal.style.display = 'flex';
                    alert('请先登录');
                }
            }
        });
    });

    loadCurrentUser();
    loadShopList();
});

async function loadCurrentUser() {
    try {
        const response = await fetch(`${API_BASE}/currentUser`, { credentials: 'include' });
        const data = await response.json();
        if (data.code === 200) {
            currentUser = data.data;
            updateUserPanel(currentUser);
        } else {
            currentUser = null;
            updateUserPanel(null);
        }
    } catch (e) {
        console.error('加载用户信息失败', e);
        currentUser = null;
        updateUserPanel(null);
    }
}

function updateUserPanel(user) {
    const avatar = document.getElementById('userPanelAvatar');
    const nameEl = document.getElementById('userPanelName');
    const infoEl = document.getElementById('userPanelInfo');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (user) {
        if (avatar && user.avatarUrl) {
            avatar.src = user.avatarUrl;
        }
        if (nameEl) nameEl.textContent = user.username;

        let roleText = '普通用户';
        if (user.userRole === 'admin') roleText = '管理员';
        else if (user.userRole === 'shop_owner') roleText = '店主';

        let html = `<li>身份：${roleText}</li>`;
        if (user.bio) html += `<li>简介：${user.bio}</li>`;
        if (user.wishlistCount !== undefined) html += `<li>愿望单：${user.wishlistCount} 件</li>`;
        if (user.userRole === 'admin' && user.pendingCount !== undefined) {
            html += `<li>待审商品：${user.pendingCount} 件</li>`;
        }
        if (infoEl) infoEl.innerHTML = html;

        if (loginBtn) loginBtn.textContent = `☰ ${user.username}`;
        if (logoutBtn) logoutBtn.style.display = 'block';
    } else {
        if (avatar) avatar.src = 'https://picsum.photos/100/100?random=avatar';
        if (nameEl) nameEl.textContent = '游客';
        if (infoEl) infoEl.innerHTML = '<li>请登录后查看</li>';
        if (loginBtn) loginBtn.textContent = '☰ 登录 / 注册';
        if (logoutBtn) logoutBtn.style.display = 'none';
    }
}

async function loadShopList() {
    const container = document.getElementById('shopListContent');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/shopList`);
        const data = await response.json();

        if (data.code === 200 && data.data && data.data.length > 0) {
            let html = '';
            data.data.forEach(shop => {
                const statusText = shop.status === 1 ? '营业中' : '休息中';
                const statusClass = shop.status === 1 ? 'shop-status-open' : 'shop-status-close';
                html += `
                    <div class="article-card" onclick="goToShopDetail(${shop.shopId})">
                        <div class="card-text">
                            <h2>${shop.shopName}</h2>
                            <p class="shop-desc">${shop.description || ''}</p>
                            <p class="shop-slogan">店主：${shop.ownerName || '未知'} | ${statusText}</p>
                        </div>
                        <div class="card-img">
                            <img src="${shop.shopImg || 'https://picsum.photos/200/150'}" alt="${shop.shopName}">
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = '<p class="placeholder-text">暂无店铺</p>';
        }
    } catch (e) {
        console.error('加载店铺列表失败', e);
        container.innerHTML = '<p class="placeholder-text" style="color:red">加载失败，请稍后重试</p>';
    }
}

function goToShopDetail(shopId) {
    window.location.href = `shopDetail.html?shopId=${shopId}`;
}
