import { defineStore } from 'pinia'
import {ref, computed, watch} from 'vue'
import client from '@/utils/api' // Giả định client axios của bạn ở đây
import { useToast } from 'vue-toastification'

const API_BASE_URL = '/api/v1/khach-hang/cong-khai/gio-hang'

export const useCartStore = defineStore('cart', () => {
  // === STATE ===
  const items = ref(JSON.parse(localStorage.getItem('cartItems') || '[]'))
  const toast = useToast()

  // Loading states
  const isFetching = ref(false)
  const isAdding = ref(false)
  const isUpdating = ref(false)
  const isRemoving = ref(false)
  const isClearing = ref(false)

  // === HELPERS ===
  // Lưu vào localStorage mỗi khi items thay đổi (chỉ khi chưa đăng nhập)
  watch(items, (newItems) => {
    if (!isLoggedIn()) {
      localStorage.setItem('cartItems', JSON.stringify(newItems))
    }
  }, { deep: true })

  function isLoggedIn() {
    return !!localStorage.getItem('authToken')
  }

  // Cache metadata (maSanPham, productViewId) theo productId (id chi tiết)
  function _getMetaCache() {
    try {
      const raw = localStorage.getItem('cartMeta')
      return raw ? JSON.parse(raw) : {}
    } catch {
      return {}
    }
  }
  function _setMetaCache(cache) {
    try {
      localStorage.setItem('cartMeta', JSON.stringify(cache))
    } catch {}
  }
  function _updateMetaCache(productDetailId, meta) {
    const cache = _getMetaCache()
    cache[String(productDetailId)] = {
      ...(cache[String(productDetailId)] || {}),
      ...meta,
    }
    _setMetaCache(cache)
  }

  function _applyMetaFromCacheToItems() {
    const cache = _getMetaCache()
    items.value.forEach(it => {
      const meta = cache[String(it.productId)]
      if (!meta) return
      if (!it.maSanPham && meta.maSanPham) it.maSanPham = meta.maSanPham
      if (!it.productViewId && meta.productViewId) it.productViewId = meta.productViewId
    })
  }

  // === PRIVATE BACKEND FUNCTIONS ===

  async function _fetchCartFromBackend() {
    const res = await client.get(API_BASE_URL)
    const currentSelectedState = new Map()
    items.value.forEach(item => {
      currentSelectedState.set(item.cartItemId, item.selected)
    })
    // Giữ lại meta cũ theo productId để merge nếu backend thiếu
    const previousMetaByProductId = new Map()
    items.value.forEach(item => {
      if (item.productId) {
        previousMetaByProductId.set(item.productId, {
          maSanPham: item.maSanPham,
          productViewId: item.productViewId,
        })
      }
    })
    const metaCache = _getMetaCache()

    items.value = res.data.map(item => {
      if (!item.id || !item.idChiTietSanPham) {
        console.error("LỖI DỮ LIỆU: Item từ backend thiếu id hoặc idChiTietSanPham.", item)
        return null
      }
      const productDetailId = item.idChiTietSanPham
      const cachedMeta = previousMetaByProductId.get(productDetailId) || metaCache[String(productDetailId)] || {}
      return {
        cartItemId: item.id,
        productId: productDetailId,
        tenSanPham: item.tenSanPham,
        image: item.image,
        price: item.price,
        color: item.color,
        size: item.size,
        quantity: item.quantity,
        selected: currentSelectedState.has(item.id) ? currentSelectedState.get(item.id) : false,
        originalPrice: item.originalPrice,
        phanTramGiam: item.phanTramGiam,
        soLuongTon: item.soLuongTon,
        maSanPham: item.maSanPham || cachedMeta.maSanPham || null,
        productViewId: cachedMeta.productViewId || null,
      }
    }).filter(item => item !== null)
    _applyMetaFromCacheToItems()
  }

  async function _addToCartBackend(item, quantity) {
    await client.post(API_BASE_URL, {
      chiTietSanPhamId: item.id,
      soLuong: quantity
    })
    await _fetchCartFromBackend() // Đồng bộ lại toàn bộ giỏ hàng
  }

  async function _updateQuantityBackend(cartItemId, quantity) {
    await client.put(`${API_BASE_URL}/${cartItemId}`, { soLuong: quantity })
    const item = items.value.find(i => i.cartItemId === cartItemId)
    if (item) item.quantity = quantity
  }

  async function _removeFromCartBackend(cartItemId) {
    await client.delete(`${API_BASE_URL}/${cartItemId}`)
    items.value = items.value.filter(item => item.cartItemId !== cartItemId)
  }


  async function _removeBatchFromBackend(cartItemIds) {
    await client.post(`${API_BASE_URL}/delete-batch`, cartItemIds);
    items.value = items.value.filter(item => !cartItemIds.includes(item.cartItemId));
  }


  // === PUBLIC ACTIONS (dùng trong components) ===

  async function fetchCart() {
    if (!isLoggedIn()) return; // Chỉ fetch khi đăng nhập
    isFetching.value = true;
    try {
      await _fetchCartFromBackend();
    } catch (error) {
      toast.error('Không thể tải giỏ hàng từ máy chủ!');
    } finally {
      isFetching.value = false;
    }
  }

  async function addToCart(product, quantity) {
    isAdding.value = true
    try {
      // Xác định id biến thể (chi tiết sản phẩm) và tồn kho tối đa
      const productDetailId = product.productId || product.id
      const maxStock = typeof product.soLuongTon === 'number' ? product.soLuongTon : undefined

      // Tìm item hiện có trong giỏ theo productId
      const existingItem = items.value.find(i => i.productId === productDetailId)

      // Tính số lượng dự kiến sau khi thêm
      const projectedQuantity = (existingItem ? existingItem.quantity : 0) + quantity

      // Nếu biết tồn kho và vượt quá, chặn thêm
      const stockLimit = typeof (existingItem && existingItem.soLuongTon) === 'number' ? existingItem.soLuongTon : maxStock
      if (typeof stockLimit === 'number' && projectedQuantity > stockLimit) {
        toast.error(`Không thể thêm quá số lượng tồn (${stockLimit}).`)
        return
      }

      if (isLoggedIn()) {
        // Lưu meta TRƯỚC khi gọi backend, vì _addToCartBackend sẽ fetch lại giỏ ngay
        _updateMetaCache(productDetailId, {
          maSanPham: product.maSanPham || null,
          productViewId: product.productViewId || product.parentProductId || null,
        })
        // Với user đã đăng nhập, vẫn chặn trước khi gọi backend dựa trên tồn kho đã biết
        await _addToCartBackend({ id: productDetailId }, quantity)
        toast.success(`Đã thêm "${product.tenSanPham}" vào giỏ hàng!`)
      } else {
        // Xử lý giỏ hàng local (cho khách)
        if (existingItem) {
          existingItem.quantity = projectedQuantity
          toast.info('Đã cập nhật số lượng trong giỏ hàng!')
        } else {
          // Sao chép đầy đủ thông tin sản phẩm để hiển thị
          const cartItem = {
            cartItemId: `local_${Date.now()}`,
            productId: productDetailId,
            tenSanPham: product.tenSanPham,
            image: product.image,
            price: product.price,
            originalPrice: product.originalPrice,
            soLuongTon: product.soLuongTon,
            quantity,
            selected: false,
            // Thêm các thuộc tính khác nếu cần
            color: product.color,
            size: product.size,
            maSanPham: product.maSanPham,
            productViewId: product.productViewId || product.parentProductId || null
          }
          items.value.push(cartItem)
          _applyMetaFromCacheToItems()
          toast.success(`Đã thêm "${product.tenSanPham}" vào giỏ hàng!`)
        }
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi thêm vào giỏ hàng!')
    } finally {
      isAdding.value = false
    }
  }

  async function updateQuantity(cartItemId, quantity) {
    isUpdating.value = true
    try {
      if (isLoggedIn()) {
        await _updateQuantityBackend(cartItemId, quantity)
      } else {
        const item = items.value.find(i => i.cartItemId === cartItemId)
        if (item) item.quantity = quantity
      }
      toast.success('Đã cập nhật số lượng!')
    } catch (error) {
      toast.error('Lỗi khi cập nhật số lượng!')
    } finally {
      isUpdating.value = false
    }
  }

  async function removeFromCart(cartItemId) {
    isRemoving.value = true
    try {
      if (isLoggedIn()) {
        await _removeFromCartBackend(cartItemId)
      } else {
        items.value = items.value.filter(item => item.cartItemId !== cartItemId)
      }
      toast.success('Đã xóa sản phẩm khỏi giỏ hàng!')
    } catch (error) {
      toast.error('Lỗi khi xóa sản phẩm!')
    } finally {
      isRemoving.value = false
    }
  }


  async function removeSelectedItemsAfterPurchase() {
    const selectedCartItemIds = selectedItems.value.map(item => item.cartItemId);
    if (selectedCartItemIds.length === 0) return;

    isRemoving.value = true;
    try {
      if (isLoggedIn()) {
        // Người dùng đăng nhập: gọi API xóa hàng loạt
        await _removeBatchFromBackend(selectedCartItemIds);
      } else {
        // Khách: chỉ xóa ở local
        items.value = items.value.filter(item => !selectedCartItemIds.includes(item.cartItemId));
      }
      // Không cần toast ở đây vì component ThanhToan sẽ hiển thị thông báo thành công chung.
    } catch (error) {
      toast.error('Lỗi khi cập nhật giỏ hàng sau khi mua!');
      // Trong trường hợp lỗi, hãy đồng bộ lại với server để đảm bảo tính nhất quán
      if (isLoggedIn()) await fetchCart();
    } finally {
      isRemoving.value = false;
    }
  }

  function toggleSelectAll(value) {
    items.value.forEach(item => { item.selected = value })
  }

  function toggleSelectItem(cartItemId) {
    const item = items.value.find(i => i.cartItemId === cartItemId)
    if (item) {
      item.selected = !item.selected
    }
  }

  // === COMPUTED PROPERTIES ===
  const selectedItems = computed(() => items.value.filter(item => item.selected))
  const selectedQuantity = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedTotal = computed(() => selectedItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0))
  const totalQuantity = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const isAllSelected = computed(() => items.value.length > 0 && items.value.every(item => item.selected))

  return {
    items,
    isLoggedIn,
    fetchCart,
    addToCart,
    updateQuantity,
    removeFromCart,
    removeSelectedItemsAfterPurchase, // Export hàm mới
    toggleSelectAll,
    toggleSelectItem,

    // States
    isFetching,
    isAdding,
    isUpdating,
    isRemoving,

    // Computed
    selectedItems,
    selectedQuantity,
    selectedTotal,
    totalQuantity,
    isAllSelected
  }
}, {
  persist: true, // Bật chế độ lưu state vào localStorage
})