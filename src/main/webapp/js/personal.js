let pendingProductId = null;

document.addEventListener('DOMContentLoaded', () => {
    initPersonalPage();
});

function initPersonalPage() {
    if (typeof currentUser !== 'undefined' && currentUser) {
        setupRoleFeatures(currentUser);
        loadWishlist();
    } else {
        const checkUser = setInterval(() => {
            if (typeof currentUser !== 'undefined' && currentUser) {
                clearInterval(checkUser);
                setupRoleFeatures(currentUser);
                loadWishlist();
            }
        }, 200);
        setTimeout(() => clearInterval(checkUser), 5000);
    }
}

function setupRoleFeatures(user) {
    const mainName = document.getElementById('mainName');
    const mainAvatar = document.getElementById('mainAvatar');
    const mainBio = document.getElementById('mainBio');
    const mainCreateTime = document.getElementById('mainCreateTime');
    const mainStats = document.getElementById('mainStats');

    if (mainName) mainName.textContent = user.username || '用户';
    if (mainAvatar && user.avatarUrl) mainAvatar.src = user.avatarUrl;
    if (mainBio) mainBio.textContent = '签名：' + (user.bio || '这个人很懒，什么都没留下');
    if (mainCreateTime) mainCreateTime.textContent = '注册时间：' + (user.createTime ? user.createTime.substring(0, 10) : '--');
    if (mainStats) mainStats.textContent = `愿望单：${user.wishlistCount || 0} 件`;

    if (user.userRole === 'admin') {
        document.getElementById('audit').style.display = 'block';
        document.getElementById('menuAudit').style.display = 'block';
        loadPendingProducts();
    }

    if (user.userRole === 'shop_owner') {
        document.getElementById('myShop').style.display = 'block';
        document.getElementById('messages').style.display = 'block';
        document.getElementById('menuShop').style.display = 'block';
        document.getElementById('menuMessages').style.display = 'block';
        loadMyShop();
        loadMessages();
    }
}

async function loadWishlist() {
    const container = document.getElementById('wishlistContent');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/wishlist`, { credentials: 'include' });
        const data = await response.json();

        if (data.code === 200 && data.data && data.data.length > 0) {
            let html = '';
            data.data.forEach(item => {
                html += `
                    <div class="wishlist-item">
                        <div class="wishlist-img">
                            <img src="${item.previewUrl || 'https://picsum.photos/60/60'}" alt="${item.name}">
                        </div>
                        <div class="wishlist-info">
                            <h4>${item.name}</h4>
                            <p>库存：${item.stock} 件</p>
                        </div>
                        <div class="wishlist-price">¥${item.price}</div>
                        <button class="remove-btn" onclick="removeFromWishlist(${item.productId})">移除</button>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = '<p style="opacity:0.6;">愿望单空空如也~</p>';
        }
    } catch (e) {
        console.error('加载愿望单失败', e);
        container.innerHTML = '<p style="color:red;">加载失败</p>';
    }
}

async function removeFromWishlist(productId) {
    if (!confirm('确定要从愿望单移除吗？')) return;

    try {
        const response = await fetch(`${API_BASE}/wishlist?productId=${productId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            loadWishlist();
            if (typeof loadCurrentUser === 'function') loadCurrentUser();
        }
    } catch (e) {
        alert('操作失败');
    }
}

async function loadPendingProducts() {
    const container = document.getElementById('auditContent');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/admin/audit`, { credentials: 'include' });
        const data = await response.json();

        if (data.code === 200 && data.data && data.data.length > 0) {
            let html = '';
            data.data.forEach(item => {
                html += `
                    <div class="audit-product-card">
                        <div class="audit-product-img">
                            <img src="${item.previewUrl || 'https://picsum.photos/80/80'}" alt="${item.name}">
                        </div>
                        <div class="audit-product-info">
                            <h4>${item.name}</h4>
                            <p>店铺：${item.shopName} | 店主：${item.ownerName}</p>
                            <p>${item.description || ''}</p>
                            <p class="audit-product-price">¥${item.price} | 库存：${item.stock}件</p>
                            <p style="font-size:12px;">提交时间：${item.uploadTime ? item.uploadTime.substring(0, 19) : ''}</p>
                        </div>
                        <div class="audit-btn-group">
                            <button class="audit-btn btn-approve" onclick="approveProduct(${item.productId})">通过</button>
                            <button class="audit-btn btn-reject" onclick="openRejectModal(${item.productId})">拒绝</button>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = '<p style="opacity:0.6;">暂无待审核商品</p>';
        }
    } catch (e) {
        console.error('加载待审核商品失败', e);
        container.innerHTML = '<p style="color:red;">加载失败</p>';
    }
}

async function approveProduct(productId) {
    if (!confirm('确定要通过该商品的审核吗？')) return;

    try {
        const formData = new FormData();
        formData.append('action', 'approve');
        formData.append('productId', productId);

        const response = await fetch(`${API_BASE}/admin/audit`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            loadPendingProducts();
            if (typeof loadCurrentUser === 'function') loadCurrentUser();
        }
    } catch (e) {
        alert('操作失败');
    }
}

function openRejectModal(productId) {
    pendingProductId = productId;
    document.getElementById('rejectReason').value = '';
    document.getElementById('rejectModal').style.display = 'flex';
}

function closeRejectModal() {
    pendingProductId = null;
    document.getElementById('rejectModal').style.display = 'none';
}

async function confirmReject() {
    const reason = document.getElementById('rejectReason').value.trim();
    if (!reason) {
        alert('请填写拒绝原因');
        return;
    }
    if (!pendingProductId) return;

    try {
        const formData = new FormData();
        formData.append('action', 'reject');
        formData.append('productId', pendingProductId);
        formData.append('rejectReason', reason);

        const response = await fetch(`${API_BASE}/admin/audit`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            closeRejectModal();
            loadPendingProducts();
            if (typeof loadCurrentUser === 'function') loadCurrentUser();
        }
    } catch (e) {
        alert('操作失败');
    }
}

async function loadMyShop() {
    const container = document.getElementById('myShopContent');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/shopOwner/product?action=shopInfo`, { credentials: 'include' });
        const data = await response.json();

        if (data.code === 200 && data.shopId) {
            container.innerHTML = `
                <a class="shop-card-link" href="shopDetail.html?shopId=${data.shopId}">
                    <h4>🏪 ${data.shopName || '我的店铺'}</h4>
                    <p>点击进入店铺详情页，管理商品</p>
                </a>
            `;
        } else {
            container.innerHTML = '<p style="opacity:0.6;">暂无店铺</p>';
        }
    } catch (e) {
        console.error('加载店铺失败', e);
        container.innerHTML = '<p style="color:red;">加载失败</p>';
    }
}

async function loadMessages() {
    const container = document.getElementById('messagesContent');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/messages`, { credentials: 'include' });
        const data = await response.json();

        if (data.code === 200 && data.data && data.data.length > 0) {
            let html = '';
            data.data.forEach(msg => {
                html += `
                    <div style="padding:12px 0; border-bottom:1px solid rgba(128,128,128,0.1);">
                        <p style="margin-bottom:5px;">${msg.content}</p>
                        <p style="font-size:12px; opacity:0.6;">${msg.createTime ? msg.createTime.substring(0, 19) : ''}</p>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = '<p style="opacity:0.6;">暂无消息</p>';
        }
    } catch (e) {
        console.error('加载消息失败', e);
        container.innerHTML = '<p style="color:red;">加载失败</p>';
    }
}
