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
    const loginBtn = document.querySelector('.login-btn'); // 确保首页有 class="login-btn" 的元素
    const modalMask = document.getElementById('loginModal');
    const modalBox = document.querySelector('.modal-box');
    const tabItems = document.querySelectorAll('.tab-item');
    const loginForm = document.querySelector('.login-form');
    const registerForm = document.querySelector('.register-form');

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
                // 发送请求到后端 (更新为 /login)
                const response = await fetch('/servletLogin/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                // 尝试解析 JSON
                const data = await response.json();

                if (data.code === 200) {
                    alert('登录成功！');
                    modalMask.style.display = 'none'; // 关闭弹窗
                    // 根据你的要求：登录成功后停留在当前页面（通过刷新页面来更新顶部的登录状态）
                    location.reload();
                } else {
                    alert(data.msg || '登录失败');
                }

            } catch (error) {
                console.error('登录错误详情:', error);
                alert('网络异常或服务器响应格式错误（请检查后端是否返回了JSON）');
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
                // 发送请求到后端 (更新为 /register)
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