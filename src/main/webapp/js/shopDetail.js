let currentShopId = null;
let isOwner = false;
let wishlistProductIds = new Set();

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
        loadWishlistIds();
    } else {
        const checkUser = setInterval(() => {
            if (typeof currentUser !== 'undefined' && currentUser) {
                clearInterval(checkUser);
                checkOwnerStatus();
                loadWishlistIds();
            }
        }, 200);
        setTimeout(() => clearInterval(checkUser), 5000);
    }
}

async function loadWishlistIds() {
    if (!currentUser) return;
    try {
        const response = await fetch(`${API_BASE}/wishlist`, { credentials: 'include' });
        const data = await response.json();
        if (data.code === 200 && data.data) {
            wishlistProductIds = new Set(data.data.map(item => item.productId));
            if (currentProducts.length > 0) {
                renderProducts(currentProducts);
            }
        }
    } catch (e) {
        console.error('加载愿望单失败', e);
    }
}

function checkOwnerStatus() {
    if (!currentUser || !currentShopId) return;

    if (currentUser.userRole === 'shop_owner' && currentUser.shopId == currentShopId) {
        isOwner = true;
        document.getElementById('ownerActions').style.display = 'block';
        if (currentProducts.length > 0) {
            renderProducts(currentProducts);
        }
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

let currentProducts = [];

function renderProducts(products) {
    const container = document.getElementById('productList');
    if (!products || products.length === 0) {
        container.innerHTML = '<p style="opacity:0.6;">暂无商品</p>';
        return;
    }

    currentProducts = products;
    let html = '';
    products.forEach((product, index) => {
        const isSoldOut = product.stock <= 0;

        if (isOwner) {
            let statusTag = '';
            if (product.auditStatus === 0) {
                statusTag = '<span style="background:#faad14;color:#fff;padding:2px 8px;border-radius:4px;font-size:12px;display:inline-block;margin-bottom:6px;">待审核</span>';
            } else if (product.auditStatus === 2) {
                statusTag = `<span style="background:#f5222d;color:#fff;padding:2px 8px;border-radius:4px;font-size:12px;display:inline-block;margin-bottom:6px;">已拒绝</span>
                    <p style="font-size:12px;color:#f5222d;margin:0 0 6px 0;">原因：${product.rejectReason || ''}</p>`;
            }

            html += `
                <div class="goods-card" data-index="${index}">
                    <div class="goods-img">
                        <img src="${product.previewUrl || 'https://picsum.photos/120/120'}" alt="${product.name}">
                    </div>
                    <div class="goods-text">
                        ${statusTag}
                        <h3>${product.name}</h3>
                        <p class="goods-desc">${product.description || ''}</p>
                        <p class="goods-price">售价：¥${product.price}</p>
                        <p class="goods-limit">库存：${product.stock} 份</p>
                    </div>
                    <div class="owner-btn-group">
                        <button class="owner-btn btn-edit">修改</button>
                        <button class="owner-btn btn-remove">下架</button>
                    </div>
                </div>
            `;
        } else {
            if (isSoldOut) {
                html += `
                    <div class="goods-card sold-out" data-index="${index}">
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
                const isReserved = wishlistProductIds.has(product.productId);
                const btnClass = isReserved ? 'reserve-btn reserved' : 'reserve-btn';
                const btnText = isReserved ? '已预约' : '预约';
                html += `
                    <div class="goods-card" data-index="${index}">
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
                            <button class="${btnClass}" ${isReserved ? 'disabled' : ''}>${btnText}</button>
                        </div>
                    </div>
                `;
            }
        }
    });

    container.innerHTML = html;
    bindProductButtons();
}

function bindProductButtons() {
    document.querySelectorAll('.reserve-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const index = this.closest('.goods-card').dataset.index;
            const product = currentProducts[index];
            addToWishlist(product.productId, product.name);
        });
    });

    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', function() {
            const index = this.closest('.goods-card').dataset.index;
            const product = currentProducts[index];
            openEditModal(product);
        });
    });

    document.querySelectorAll('.btn-remove').forEach(btn => {
        btn.addEventListener('click', function() {
            const index = this.closest('.goods-card').dataset.index;
            const product = currentProducts[index];
            removeProduct(product.productId, product.name);
        });
    });
}

async function addToWishlist(productId, productName) {
    if (!currentUser) {
        alert('请先登录');
        document.getElementById('loginModal').style.display = 'flex';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/wishlist`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + encodeURIComponent(productId)
        });
        const data = await response.json();
        alert(data.msg);
        if (data.code === 200) {
            wishlistProductIds.add(productId);
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
        let body = 'name=' + encodeURIComponent(name) +
            '&price=' + encodeURIComponent(price) +
            '&stock=' + encodeURIComponent(stock) +
            '&description=' + encodeURIComponent(description) +
            '&previewUrl=' + encodeURIComponent(previewUrl);

        let url = `${API_BASE}/shopOwner/product`;
        if (productId) {
            body += '&action=update&productId=' + encodeURIComponent(productId);
        } else {
            body += '&action=add';
        }

        const response = await fetch(url, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body
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
        const response = await fetch(`${API_BASE}/shopOwner/product`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=remove&productId=' + encodeURIComponent(productId)
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
