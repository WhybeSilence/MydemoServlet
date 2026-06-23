let currentShopId = null;
let isOwner = false;

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    currentShopId = params.get('shopId');

    if (!currentShopId) {
        document.getElementById('productList').innerHTML = '<p style="color:red;">缺少店铺ID</p>';
        return;
    }

    initShopDetail();
});

function initShopDetail() {
    loadShopDetail();

    if (typeof currentUser !== 'undefined' && currentUser) {
        checkOwnerStatus();
    } else {
        const checkUser = setInterval(() => {
            if (typeof currentUser !== 'undefined' && currentUser) {
                clearInterval(checkUser);
                checkOwnerStatus();
            }
        }, 200);
        setTimeout(() => clearInterval(checkUser), 5000);
    }
}

function checkOwnerStatus() {
    if (!currentUser || !currentShopId) return;

    if (currentUser.userRole === 'shop_owner' && currentUser.shopId == currentShopId) {
        isOwner = true;
        document.getElementById('ownerActions').style.display = 'block';
    }
}

async function loadShopDetail() {
    try {
        const response = await fetch(`${API_BASE}/shopDetail?shopId=${currentShopId}`);
        const data = await response.json();

        if (data.code === 200 && data.data) {
            const shop = data.data.shop;
            const products = data.data.products;

            document.getElementById('shopName').textContent = shop.shopName;
            document.getElementById('shopDesc').textContent = shop.description;
            document.getElementById('shopOwner').textContent = shop.ownerName || '未知';

            const statusEl = document.getElementById('shopStatus');
            if (shop.status === 1) {
                statusEl.textContent = '营业中';
                statusEl.style.backgroundColor = '#52c41a';
            } else {
                statusEl.textContent = '休息中';
                statusEl.style.backgroundColor = '#f5222d';
            }

            renderProducts(products);
        } else {
            document.getElementById('productList').innerHTML = '<p style="color:red;">加载失败</p>';
        }
    } catch (e) {
        console.error('加载店铺详情失败', e);
        document.getElementById('productList').innerHTML = '<p style="color:red;">加载失败</p>';
    }
}

function renderProducts(products) {
    const container = document.getElementById('productList');
    if (!products || products.length === 0) {
        container.innerHTML = '<p style="opacity:0.6;">暂无商品</p>';
        return;
    }

    let html = '';
    products.forEach(product => {
        const isSoldOut = product.stock <= 0;

        if (isOwner) {
            html += `
                <div class="goods-card">
                    <div class="goods-img">
                        <img src="${product.previewUrl || 'https://picsum.photos/120/120'}" alt="${product.name}">
                    </div>
                    <div class="goods-text">
                        <h3>${product.name}</h3>
                        <p class="goods-desc">${product.description || ''}</p>
                        <p class="goods-price">售价：¥${product.price}</p>
                        <p class="goods-limit">库存：${product.stock} 份</p>
                    </div>
                    <div class="owner-btn-group">
                        <button class="owner-btn btn-edit" onclick='openEditModal(${JSON.stringify(product)})'>修改</button>
                        <button class="owner-btn btn-remove" onclick="removeProduct(${product.productId}, '${product.name}')">下架</button>
                    </div>
                </div>
            `;
        } else {
            if (isSoldOut) {
                html += `
                    <div class="goods-card sold-out">
                        <div class="goods-img">
                            <img src="${product.previewUrl || 'https://picsum.photos/120/120'}" alt="${product.name}">
                        </div>
                        <div class="goods-text">
                            <h3>${product.name}</h3>
                            <p class="goods-desc">${product.description || ''}</p>
                            <p class="goods-price">售价：¥${product.price}</p>
                            <p class="goods-limit">库存：${product.stock} 份</p>
                        </div>
                        <div class="sold-tag">已售罄</div>
                    </div>
                `;
            } else {
                html += `
                    <div class="goods-card">
                        <div class="goods-img">
                            <img src="${product.previewUrl || 'https://picsum.photos/120/120'}" alt="${product.name}">
                        </div>
                        <div class="goods-text">
                            <h3>${product.name}</h3>
                            <p class="goods-desc">${product.description || ''}</p>
                            <p class="goods-price">售价：¥${product.price}</p>
                            <p class="goods-limit">库存：${product.stock} 份</p>
                        </div>
                        <div class="goods-btn-group">
                            <button class="reserve-btn" onclick="addToWishlist(${product.productId}, '${product.name}')">预约</button>
                        </div>
                    </div>
                `;
            }
        }
    });

    container.innerHTML = html;
}

async function addToWishlist(productId, productName) {
    if (!currentUser) {
        alert('请先登录');
        document.getElementById('loginModal').style.display = 'flex';
        return;
    }

    try {
        const formData = new FormData();
        formData.append('productId', productId);

        const response = await fetch(`${API_BASE}/wishlist`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            loadShopDetail();
            if (typeof loadCurrentUser === 'function') loadCurrentUser();
        }
    } catch (e) {
        alert('操作失败');
    }
}

function openAddModal() {
    document.getElementById('productModalTitle').textContent = '上架商品';
    document.getElementById('editProductId').value = '';
    document.getElementById('productName').value = '';
    document.getElementById('productPrice').value = '';
    document.getElementById('productStock').value = '';
    document.getElementById('productDesc').value = '';
    document.getElementById('productPreviewUrl').value = '';
    document.getElementById('productModal').style.display = 'flex';
}

function openEditModal(product) {
    document.getElementById('productModalTitle').textContent = '修改商品';
    document.getElementById('editProductId').value = product.productId;
    document.getElementById('productName').value = product.name;
    document.getElementById('productPrice').value = product.price;
    document.getElementById('productStock').value = product.stock;
    document.getElementById('productDesc').value = product.description || '';
    document.getElementById('productPreviewUrl').value = product.previewUrl || '';
    document.getElementById('productModal').style.display = 'flex';
}

function closeProductModal() {
    document.getElementById('productModal').style.display = 'none';
}

async function submitProduct() {
    const productId = document.getElementById('editProductId').value;
    const name = document.getElementById('productName').value.trim();
    const price = document.getElementById('productPrice').value;
    const stock = document.getElementById('productStock').value;
    const description = document.getElementById('productDesc').value.trim();
    const previewUrl = document.getElementById('productPreviewUrl').value.trim();

    if (!name) {
        alert('请输入商品名称');
        return;
    }
    if (!price) {
        alert('请输入价格');
        return;
    }
    if (!stock) {
        alert('请输入库存数量');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('name', name);
        formData.append('price', price);
        formData.append('stock', stock);
        formData.append('description', description);
        formData.append('previewUrl', previewUrl);

        let url, action;
        if (productId) {
            action = 'update';
            formData.append('action', 'update');
            formData.append('productId', productId);
            url = `${API_BASE}/shopOwner/product`;
        } else {
            action = 'add';
            formData.append('action', 'add');
            url = `${API_BASE}/shopOwner/product`;
        }

        const response = await fetch(url, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            closeProductModal();
            loadShopDetail();
        }
    } catch (e) {
        alert('操作失败');
    }
}

async function removeProduct(productId, productName) {
    if (!confirm(`确定要下架商品【${productName}】吗？`)) return;

    try {
        const formData = new FormData();
        formData.append('action', 'remove');
        formData.append('productId', productId);

        const response = await fetch(`${API_BASE}/shopOwner/product`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            loadShopDetail();
        }
    } catch (e) {
        alert('操作失败');
    }
}
