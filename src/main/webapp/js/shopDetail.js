// 预约按钮：点击切换 预约 / 已预约
const reserveBtns = document.querySelectorAll('.reserve-btn');
reserveBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (this.classList.contains('reserved')) {
            // 已预约 → 取消
            this.classList.remove('reserved');
            this.innerText = '预约';
        } else {
            // 未预约 → 设为已预约
            this.classList.add('reserved');
            this.innerText = '已预约';
        }
    });
});

// 购物车按钮：点击切换 加入购物车 / 已加入购物车
const cartBtns = document.querySelectorAll('.cart-btn');
cartBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (this.classList.contains('added')) {
            // 已加入 → 取消
            this.classList.remove('added');
            this.innerText = '加入购物车';
        } else {
            // 未加入 → 设为已加入
            this.classList.add('added');
            this.innerText = '已加入购物车';
        }
    });
});



// 1. 店铺营业/打烊切换
const shopSwitchBtn = document.querySelector('.shop-switch-btn');
const shopStatusText = document.querySelector('.shop-status');

shopSwitchBtn.addEventListener('click', function () {
    if (document.body.classList.contains('shop-close')) {
        // 当前打烊 → 切换为营业中
        document.body.classList.remove('shop-close');
        shopStatusText.innerText = '营业中';
        shopStatusText.style.backgroundColor = '#52c41a';
        this.innerText = '打烊';
    } else {
        // 当前营业中 → 切换为打烊
        document.body.classList.add('shop-close');
        shopStatusText.innerText = '已打烊';
        shopStatusText.style.backgroundColor = '#f5222d';
        this.innerText = '营业';
    }
});

// 2. 预约按钮：点击切换 预约 / 已预约（打烊状态自动禁用）
const reserveBtns = document.querySelectorAll('.reserve-btn');
reserveBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (this.classList.contains('reserved')) {
            this.classList.remove('reserved');
            this.innerText = '预约';
        } else {
            this.classList.add('reserved');
            this.innerText = '已预约';
        }
    });
});

// 3. 购物车按钮：点击切换 加入购物车 / 已加入购物车（打烊状态自动禁用）
const cartBtns = document.querySelectorAll('.cart-btn');
cartBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (this.classList.contains('added')) {
            this.classList.remove('added');
            this.innerText = '加入购物车';
        } else {
            this.classList.add('added');
            this.innerText = '已加入购物车';
        }
    });
});