// ================== home.js 最终修复版 ==================

// 等待页面所有元素加载完毕后再执行脚本，防止找不到 ID
document.addEventListener('DOMContentLoaded', () => {

    // ================== 1. 基础视觉交互 ==================
    const bgImages = [
        "https://picsum.photos/id/1015/1920/1080",
        "https://picsum.photos/id/1016/1920/1080",
        "https://picsum.photos/id/1018/1920/1080",
        "https://picsum.photos/id/1019/1920/1080"
    ];
    const randomBg = bgImages[Math.floor(Math.random() * bgImages.length)];
    document.body.style.backgroundImage = `url(${randomBg})`;

    // 日间/黑夜模式切换
    const modeToggle = document.getElementById('modeToggle');
    if(modeToggle){
        modeToggle.addEventListener('change', () => {
            document.body.classList.toggle('dark');
        });
    }

    // 右侧用户面板逻辑
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

    // 收藏按钮逻辑
    document.querySelectorAll('.collect-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            this.classList.toggle('collected');
            this.innerText = this.classList.contains('collected') ? '★ 已收藏' : '☆ 收藏';
        });
    });

    // ================== 登录弹窗逻辑 ==================
    const loginBtn = document.getElementById('loginBtn');
    const userInfoBar = document.getElementById('userInfoBar');
    const welcomeText = document.getElementById('welcomeText');
    const logoutBtn = document.getElementById('logoutBtn');
    const modalMask = document.getElementById('loginModal');
    const modalBox = document.querySelector('.modal-box');
    const tabItems = document.querySelectorAll('.tab-item');
    const loginForm = document.querySelector('.login-form');
    const registerForm = document.querySelector('.register-form');

    // 检查登录状态
    checkLoginStatus();

    if(loginBtn && modalMask){
        loginBtn.addEventListener('click', () => {
            modalMask.style.display = 'flex';
        });

        modalMask.addEventListener('click', (e) => {
            if (!modalBox.contains(e.target)) {
                modalMask.style.display = 'none';
            }
        });
    }

    // 退出登录
    if(logoutBtn){
        logoutBtn.addEventListener('click', async () => {
            try {
                const response = await fetch('/servletLogin/logout', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                });
                // 刷新页面显示未登录状态
                window.location.reload();
            } catch (error) {
                console.error('退出登录失败:', error);
                window.location.reload();
            }
        });
    }

    // 检查登录状态
    function checkLoginStatus() {
        fetch('/servletLogin/checkLogin', {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.username) {
                // 已登录状态
                loginBtn.style.display = 'none';
                userInfoBar.style.display = 'flex';
                welcomeText.innerText = `欢迎, ${data.username}`;
            } else {
                // 未登录状态
                loginBtn.style.display = 'block';
                userInfoBar.style.display = 'none';
            }
        })
        .catch(error => {
            console.error('检查登录状态失败:', error);
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

    // ================== 目录跳转 ==================
    document.querySelectorAll('.sidebar-card a').forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });


    // ================== 核心：登录与注册提交逻辑 ==================

    // 1. 获取按钮元素
    const loginSubmitBtn = document.getElementById('loginSubmitBtn');
    const registerSubmitBtn = document.getElementById('registerSubmitBtn');

    // 2. 检查按钮是否存在，不存在则打印警告，防止报错阻断后续代码
    if (!loginSubmitBtn) console.warn("未找到登录按钮 #loginSubmitBtn");
    if (!registerSubmitBtn) console.warn("未找到注册按钮 #registerSubmitBtn");

    // --- 处理登录请求 ---
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
                const response = await fetch('/servletLogin/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert(data.msg || '登录成功！');
                    modalMask.style.display = 'none';
                    // 刷新当前页面显示登录状态
                    window.location.reload();
                } else {
                    alert(data.msg || '登录失败');
                }

            } catch (error) {
                console.error('登录错误详情:', error);
                alert('网络异常或服务器响应格式错误');
            }
        });
    }

    // --- 处理注册请求 ---
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
                const response = await fetch('/servletLogin/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert('注册成功！请登录');
                    // 切换到登录 Tab
                    document.querySelector('[data-tab="login"]').click();
                    // 清空表单
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
});