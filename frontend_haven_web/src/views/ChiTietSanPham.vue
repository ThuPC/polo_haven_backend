<template>
  <div class="product-detail-page">
    <!-- Hero Section (Breadcrumb) -->
    <section class="hero-section py-3 bg-light">
      <div class="container">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb fs-6">
            <li class="breadcrumb-item">
              <router-link to="/">Trang chủ</router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link to="/products">Sản phẩm</router-link>
            </li>
            <li class="breadcrumb-item active" aria-current="page">
              {{ product.tenSanPham }}
            </li>
          </ol>
        </nav>
      </div>
    </section>

    <!-- Product Detail Section -->
    <section class="product-detail-section py-5">
      <div class="container">
        <div class="row g-4">
          <!-- Product Image Slideshow -->
          <div class="col-12 col-md-6">
            <!-- Dùng v-if để đảm bảo chỉ render carousel khi có dữ liệu ảnh -->
            <div v-if="product && product.imageList && product.imageList.length > 0" class="carousel slide"
                 id="productImageCarousel">

              <!-- Carousel Indicators (Nút tròn nhỏ bên dưới) -->
              <div class="carousel-indicators">
                <button v-for="(image, index) in product.imageList" :key="index" type="button"
                        data-bs-target="#productImageCarousel" :data-bs-slide-to="index" :class="{ 'active': index === 0 }"
                        :aria-label="'Slide ' + (index + 1)" :aria-current="index === 0 ? 'true' : null"></button>
              </div>

              <!-- Carousel Inner (Phần chứa các ảnh) -->
              <div class="carousel-inner">
                <div v-for="(image, index) in product.imageList" :key="index" class="carousel-item"
                     :class="{ 'active': index === 0 }">

                  <!-- === THAY ĐỔI QUAN TRỌNG NHẤT NẰM Ở ĐÂY === -->
                  <!-- Sử dụng trực tiếp biến 'image' vì nó đã là một URL đầy đủ -->
                  <img :src="image" :alt="`${product.tenSanPham} - Ảnh ${index + 1}`"
                       class="d-block w-100 object-fit-cover" style="height: 500px;" />

                </div>
              </div>

              <!-- Carousel Controls (Nút Previous/Next) -->
              <button class="carousel-control-prev" type="button" data-bs-target="#productImageCarousel"
                      data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Previous</span>
              </button>
              <button class="carousel-control-next" type="button" data-bs-target="#productImageCarousel"
                      data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Next</span>
              </button>
            </div>

            <!-- (Tùy chọn) Hiển thị ảnh placeholder nếu sản phẩm không có ảnh -->
            <div v-else>
              <img src="" :alt="product.tenSanPham" class="d-block w-100 object-fit-cover"
                   style="height: 500px;" />
            </div>
          </div>

          <!-- Product Info -->
          <div class="col-12 col-md-6">
            <div
                class="product-info animate__animated animate__fadeInUp p-4 bg-white rounded shadow-sm position-relative">
              <!-- Product Name + Discount Badge (inline) -->
              <div class="d-flex align-items-center gap-2 mb-2">
                <h3 class="product-name card-title fs-3 fw-bold mb-0">
                  {{ product.tenSanPham }}
                </h3>
                <span v-if="product.phanTramGiam > 0"
                      class="discount-badge badge bg-danger text-white fs-6">
                  -{{ product.phanTramGiam }}%
                </span>
              </div>

              <!-- Product Code -->
              <div class="product-code mb-3">
                <small class="text-muted fw-semibold">Mã sản phẩm:</small>
                <span class="ms-2 text-primary fw-bold">{{ product.maSanPham || 'N/A' }}</span>
              </div>

              <!-- Price Range (Dynamic, red color) -->
              <div class="product-price fs-3 mb-2 d-flex align-items-center gap-2">
                <template v-if="selectedColor && selectedSize && currentPrice !== 'Giá không khả dụng'">
                  <span v-if="product.phanTramGiam > 0 && originalPrice"
                        class="original-price text-muted text-decoration-line-through me-2">
                    {{ originalPrice }}
                  </span>
                  <span class="current-price text-danger">
                    {{ currentPrice }}
                  </span>
                </template>
                <template v-else>
                  <span class="current-price text-danger">
                    {{ formatPriceRange(product.khoangGia) }}
                  </span>
                </template>
              </div>

              <!-- Color Selection -->
              <div class="product-details mb-3">
                <p class="mb-3">
                  <small class="text-muted fw-semibold">Màu sắc:</small>
                  <span v-if="product.mauSacList && product.mauSacList.length > 0" class="d-flex gap-2 mt-2">
                    <button v-for="color in product.mauSacList" :key="color.id"
                            class="color-dot rounded-circle border-2"
                            :class="{ 'active': selectedColor === color.id }"
                            :style="{ backgroundColor: color.maMauSac.toLowerCase() }" @click="selectColor(color.id)"
                            :aria-label="`Chọn màu ${color.maMauSac}`">
                    </button>
                  </span>
                  <span v-if="product.mauSacList && product.mauSacList.length > 0"  class="d-flex gap-2 mt-2">
                            {{product.mauSacList.map(color => color.ten).join(' ')}}
                  </span>
                  <span v-else class="ms-1 text-muted">Đang cập nhật</span>
                </p>
              </div>

              <!-- Size Selection -->
              <div class="product-details mb-3">
                <p class="mb-3">
                  <small class="text-muted fw-semibold">Kích thước:</small>
                  <span v-if="product.kichThuocList && product.kichThuocList.length > 0" class="d-flex gap-2 mt-2">
                    <button v-for="size in product.kichThuocList" :key="size.id"
                            class="btn btn-outline-secondary btn-sm rounded-pill"
                            :class="{ 'btn-secondary text-white': selectedSize === size.id }" @click="selectSize(size.id)">
                      {{ size.size }}
                    </button>
                  </span>
                  <span v-else class="ms-1 text-muted">Đang cập nhật</span>
                </p>
              </div>

              <!-- Quantity Input and Stock -->
              <div class="product-details mb-2">
                <div class="mb-3">
                  <small class="text-muted fw-semibold">Số lượng:</small>
                  <div class="d-flex align-items-center gap-3 mt-2">
                    <div class="input-group input-group-sm quantity-group" style="width: 120px;">
                      <button class="btn btn-outline-secondary" type="button" @click="decreaseQuantity"
                              :disabled="quantity <= 1" aria-label="Giảm số lượng">
                        <i class="bi bi-dash"></i>
                      </button>
                      <input id="quantity" v-model.number="quantity" type="number" min="1" :max="maxQuantity"
                             class="form-control text-center" @change="validateQuantity" aria-label="Số lượng sản phẩm" />
                      <button class="btn btn-outline-secondary" type="button" @click="increaseQuantity"
                              :disabled="quantity >= maxQuantity" aria-label="Tăng số lượng">
                        <i class="bi bi-plus"></i>
                      </button>
                    </div>
                    <!-- Số lượng đã bán -->
                    <div class="sold-quantity-info">
                      <small class="text-muted">
                        <i class="bi bi-graph-up me-1"></i>
                        Đã bán: <span class="fw-bold text-success">{{ product.soLuongDaBan || 0 }}</span>
                      </small>
                    </div>
                  </div>
                </div>
                <div v-if="quantityError" class="text-danger fs-6 mt-2">{{ quantityError }}</div>
                <div class="mt-2 fs-5 fw-semibold">
                  <i class="bi bi-box-seam me-1"></i>
                  <span :class="stockClass">Còn {{ maxQuantity }} sản phẩm</span>
                </div>

              </div>

              <!-- Add to Cart and Buy Now Buttons -->
              <div class="product-details mb-2">
                <p class="mb-3">
                  <span class="d-flex gap-2 mt-2">
                    <button
                        class="btn btn-success btn-sm rounded-pill"
                        @click="addToCart"
                        :disabled="cartStore.isAdding || !selectedSize || !selectedColor"
                    >
                      <span v-if="cartStore.isAdding" class="spinner-border spinner-border-sm me-2"></span>
                      <i class="bi bi-cart-plus me-2"></i> Thêm vào giỏ hàng
                    </button>
                    <button
                        class="btn btn-primary btn-sm rounded-pill"
                        @click="buyNow"
                        :disabled="cartStore.isAdding || !selectedSize || !selectedColor"
                    >
                      <span v-if="cartStore.isAdding" class="spinner-border spinner-border-sm me-2"></span>
                      <i class="bi bi-bag-check me-2"></i> Mua ngay
                    </button>
                  </span>
                </p>
              </div>

              <!-- Mô tả chi tiết chuyển thành accordion Bootstrap -->
              <div class="accordion mt-3" id="productDetailAccordion">
                <div class="accordion-item">
                  <h2 class="accordion-header" id="headingDesc">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseDesc" aria-expanded="false" aria-controls="collapseDesc">
                      Mô tả chi tiết sản phẩm
                    </button>
                  </h2>
                  <div id="collapseDesc" class="accordion-collapse collapse" aria-labelledby="headingDesc" data-bs-parent="#productDetailAccordion">
                    <div class="accordion-body">
                      <div class="text-muted">
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Tên sản phẩm: </small>
                          {{ product.tenSanPham }}
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Chất liệu: </small>
                          {{ product.chatLieu || 'Đang cập nhật' }}
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Màu sắc: </small>
                          <span v-if="product.mauSacList && product.mauSacList.length > 0">
                            {{product.mauSacList.map(color => color.ten).join(', ')}}
                          </span>
                          <span v-else>Đang cập nhật</span>
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Thương hiệu:</small>
                          {{ product.thuongHieu || 'Đang cập nhật' }}
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Xuất xứ:</small>
                          {{ product.xuatXu || 'Đang cập nhật' }}
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Kích thước:</small>
                          <span v-if="product.kichThuocList && product.kichThuocList.length > 0">
                            {{product.kichThuocList.map(size => size.size).join(', ')}}
                          </span>
                          <span v-else>Đang cập nhật</span>
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Số lượng đã bán:</small>
                          {{ product.soLuongDaBan || 0 }}
                        </p>
                        <p class="mb-2">
                          <small class="text-muted fw-semibold">Mô tả:</small>
                          {{ product.moTa || 'Đang cập nhật' }}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Related Products Section (Slideshow) -->
    <section class="related-products py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Sản phẩm liên quan</h2>
          <p class="section-subtitle text-muted fs-5">Khám phá thêm những mẫu áo polo khác</p>
        </div>
        <div id="relatedProductsCarousel" class="carousel slide">
          <div class="carousel-inner">
            <div v-for="(chunk, chunkIndex) in chunkedRelatedProducts" :key="chunkIndex" class="carousel-item" :class="{ active: chunkIndex === 0 }">
              <div class="row g-4">
                <div v-for="product in chunk" :key="product.id" class="col-12 col-sm-6 col-md-3">
                  <div class="product-card card h-100 shadow-sm cursor-pointer animate__animated animate__zoomIn">
                    <div class="product-image position-relative overflow-hidden" style="height: 250px;">
                      <img :src="product.image || product.imageList?.[0] || '/src/assets/ao1.webp'" :alt="product.tenSanPham" class="card-img-top object-fit-cover" />
                      <div v-if="product.phanTramGiam > 0"
                           class="product-badge position-absolute top-0 start-0 bg-danger text-white px-3 py-1 rounded-pill fs-6">
                        Sale
                      </div>
                    </div>
                    <div class="product-info card-body">
                      <h3 class="product-name card-title fs-6 fw-semibold">
                        {{ product.tenSanPham }}
                        <span v-if="product.phanTramGiam > 0" class="text-danger fw-bold ms-2">
                          -{{ product.phanTramGiam }}%
                        </span>
                      </h3>
                      <div class="product-details mb-2">
                        <p class="mb-1">
                          <small class="text-muted">Màu sắc:</small>
                          <span v-for="(color, cIdx) in product.mauSacList" :key="cIdx" class="color-dot ms-1"
                                :style="{ backgroundColor: color.maMauSac }"></span>
                        </p>
                        <p class="mb-0">
                          <small class="text-muted">Size:</small>
                          {{ product.kichThuocList.map(item => item.size).join(', ') }}
                        </p>
                      </div>
                      <div class="product-price fs-5 fw-semibold">
                        <span class="current-price text-success">{{ formatPriceRange(product.khoangGia) }}</span>
                      </div>
                    </div>
                    <div
                        class="product-overlay d-flex align-items-center justify-content-center position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 transition-opacity">
                      <router-link :to="`/product/${product.id}`" class="btn btn-success rounded-pill shadow">
                        Xem chi tiết
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#relatedProductsCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#relatedProductsCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import 'bootstrap/dist/css/bootstrap.min.css'
import { getSanPhamChiTiet, getListSanPham } from '@/services/sanPham'
import { getListSanPhamChiTiet } from '@/services/spct'
import { useCartStore } from '@/stores/cart'
import { useToast } from 'vue-toastification'

const cartStore = useCartStore()
const toast = useToast()

const route = useRoute()

const props = defineProps({
  id: {
    type: String,
    required: false
  }
})
const productId = computed(() => props.id || route.params.id)

const product = ref({
  id: '',
  tenSanPham: '',
  moTa: '',
  thuongHieu: '',
  chatLieu: '',
  xuatXu: '',
  khoangGia: '',
  imageList: [],
  mauSacList: [],
  kichThuocList: [],
  variants: [],
  soLuongTon: null,
  phanTramGiam: 0,
  soLuongDaBan: 0
})

const relatedProducts = ref([])

const selectedSize = ref('')
const selectedColor = ref('')
const quantity = ref(1)
const quantityError = ref('')

const currentPrice = computed(() => {
  if (selectedColor.value && selectedSize.value) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    if (variant) {
      const giaKhuyenMai = variant.donGia
      return formatPrice(giaKhuyenMai)
    }
    return 'Giá không khả dụng'
  }
  return formatPriceRange(product.value.khoangGia)
})

const originalPrice = computed(() => {
  if (selectedColor.value && selectedSize.value && product.value.phanTramGiam > 0) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    if (variant && variant.giaGoc) {
      return formatPrice(variant.giaGoc)
    }
  }
  return null
})

const stockClass = computed(() => {
  if (maxQuantity.value < 10) return 'text-danger'
  if (maxQuantity.value < 50) return 'text-warning'
  return 'text-success'
})

const relatedProductGroups = computed(() => {
  const groups = []
  for (let i = 0; i < relatedProducts.value.length; i += 4) {
    groups.push(relatedProducts.value.slice(i, i + 4))
  }
  return groups
})

const selectSize = (sizeId) => {
  selectedSize.value = selectedSize.value === sizeId ? '' : sizeId
}

const selectColor = (colorId) => {
  selectedColor.value = selectedColor.value === colorId ? '' : colorId
}

const maxQuantity = computed(() => {
  if (selectedColor.value && selectedSize.value) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    return variant ? variant.soLuongTon : "hãy chọn kích thước và màu sắc"
  }
  return "hãy chọn kích thước và màu sắc"
})

const validateQuantity = () => {
  quantityError.value = ''

  if (quantity.value < 1) {
    quantity.value = 1
    quantityError.value = 'Số lượng phải lớn hơn 0'
  } else if (quantity.value > maxQuantity.value) {
    quantity.value = maxQuantity.value
    quantityError.value = `Số lượng không được vượt quá ${maxQuantity.value}`
  }

  // Kiểm tra thêm: số lượng trong giỏ hàng + số lượng mới không vượt quá tồn kho
  if (selectedColor.value && selectedSize.value) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    if (variant) {
      // Tìm sản phẩm đã có trong giỏ hàng
      const existingCartItem = cartStore.items.find(item => item.id === variant.id)
      const currentCartQuantity = existingCartItem ? existingCartItem.quantity : 0
      const totalQuantity = currentCartQuantity + quantity.value

      if (totalQuantity > variant.soLuongTon) {
        const availableQuantity = variant.soLuongTon - currentCartQuantity
        if (availableQuantity <= 0) {
          quantityError.value = 'Sản phẩm này đã hết hàng trong giỏ hàng!'
          quantity.value = 0
        } else {
          quantityError.value = `Chỉ có thể thêm tối đa ${availableQuantity} sản phẩm (đã có ${currentCartQuantity} trong giỏ hàng)`
          quantity.value = availableQuantity
        }
      }
    }
  }
}

const increaseQuantity = () => {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
    validateQuantity()
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
    validateQuantity()
  }
}

const addToCart = () => {
  console.log('addToCart called', selectedColor.value, selectedSize.value, quantity.value)

  // Validate trước khi thêm
  validateQuantity()
  if (quantityError.value) {
    toast.error(quantityError.value)
    return
  }

  if (selectedSize.value && selectedColor.value) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    if (variant) {
      // Kiểm tra lại tồn kho trước khi thêm
      const existingCartItem = cartStore.items.find(item => item.id === variant.id)
      const currentCartQuantity = existingCartItem ? existingCartItem.quantity : 0
      const totalQuantity = currentCartQuantity + quantity.value

      if (totalQuantity > variant.soLuongTon) {
        toast.error(`Không thể thêm sản phẩm. Tổng số lượng vượt quá tồn kho (${variant.soLuongTon})`)
        return
      }

      const selectedColorObj = product.value.mauSacList.find(c => c.id === selectedColor.value)
      const selectedSizeObj = product.value.kichThuocList.find(s => s.id === selectedSize.value)
      const cartItem = {
        id: variant.id,
        tenSanPham: product.value.tenSanPham,
        price: variant.donGia,
        originalPrice: variant.giaGoc,
        color: selectedColorObj ? selectedColorObj.ten : 'Unknown',
        size: selectedSizeObj ? selectedSizeObj.size : 'Unknown',
        image: Array.isArray(product.value.imageList) && product.value.imageList.length > 0 ? product.value.imageList[0] : '/src/assets/ao1.webp',
        quantity: quantity.value,
        phanTramGiam: product.value.phanTramGiam || 0,
        soLuongTon: variant.soLuongTon, // Thêm thông tin tồn kho
        maSanPham: product.value.maSanPham,
        // Lưu id sản phẩm cha để điều hướng từ giỏ hàng/checkout
        productViewId: product.value.id
      }

      try {
        cartStore.addToCart(cartItem, quantity.value)
      } catch (error) {
        console.error('Lỗi khi thêm vào giỏ hàng:', error)
        toast.error('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng!')
      }
    } else {
      toast.error('Không tìm thấy biến thể phù hợp. Vui lòng chọn lại màu sắc và kích thước.')
    }
  } else {
    toast.info('Vui lòng chọn cả màu sắc và kích thước trước khi thêm vào giỏ hàng!')
  }
}

const buyNow = () => {
  if (selectedSize.value && selectedColor.value) {
    const variant = product.value.variants.find(
        v => v.colorId === selectedColor.value && v.sizeId === selectedSize.value
    )
    if (variant) {
      toast.success(`Đã mua ${quantity.value} x "${product.value.tenSanPham}" ngay lập tức!`)
    } else {
      toast.error('Không tìm thấy biến thể phù hợp. Vui lòng chọn lại màu sắc và kích thước.')
    }
  } else {
    toast.info('Vui lòng chọn cả màu sắc và kích thước trước khi mua ngay!')
  }
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(price)
}

const formatPriceRange = (priceString, locale = 'vi-VN', currency = 'VND') => {
  if (!priceString || typeof priceString !== 'string' || !priceString.includes(' - ')) {
    const singlePrice = parseFloat(priceString)
    if (!isNaN(singlePrice)) {
      return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currency,
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(singlePrice)
    }
    return 'N/A'
  }
  const [minPriceStr, maxPriceStr] = priceString.split(' - ')
  const minPrice = parseFloat(minPriceStr)
  const maxPrice = parseFloat(maxPriceStr)
  if (isNaN(minPrice) || isNaN(maxPrice)) {
    return 'Giá không hợp lệ'
  }
  const formatter = new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })
  if (minPrice === maxPrice) {
    return formatter.format(minPrice)
  }
  return `${formatter.format(minPrice)} - ${formatter.format(maxPrice)}`
}

const fetchProductDetail = async () => {
  try {
    const detailResponse = await getSanPhamChiTiet(productId.value)
    const productData = detailResponse.data
    const variantResponse = await getListSanPhamChiTiet()
    const variantData = Array.isArray(variantResponse.data) ? variantResponse.data : [variantResponse.data]
    const variants = variantData
        .map(variant => {
          const color = productData.mauSacList.find(c => c.ten.toLowerCase() === variant.mauSac.toLowerCase())
          const size = productData.kichThuocList.find(s => String(s.size) === String(variant.kichThuoc))
          const phanTramGiam = productData.phanTramGiam || 0
          const giaGoc = variant.donGia
          const giaSauGiam = phanTramGiam > 0 ? Math.round(giaGoc * (1 - phanTramGiam / 100)) : giaGoc

          return {
            id: variant.id, // <-- Đảm bảo có dòng này!
            colorId: color ? color.id : null,
            sizeId: size ? size.id : null,
            donGia: giaSauGiam, // giá sau khuyến mãi (đã tính giảm)
            giaGoc: giaGoc,     // giá gốc (chưa giảm)
            soLuongTon: variant.soLuong // SỬA DÒNG NÀY
          }
        })
        .filter(v => v.id && v.colorId && v.sizeId)

    product.value = {
      ...productData,
      variants,
      imageList: productData.imageList.length > 0 ? productData.imageList : ['/src/assets/ao1.webp']
    }
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm', error)
    toast.error('Không thể tải chi tiết sản phẩm. Vui lòng thử lại sau.')
  }
}

const fetchRelatedProducts = async () => {
  try {
    const response = await getListSanPham()
    relatedProducts.value = response.data.filter(p => p.id !== productId.value).slice(0, 8)
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm liên quan', error)
    toast.error('Không thể tải sản phẩm liên quan!')
  }
}

onMounted(() => {
  fetchProductDetail()
  fetchRelatedProducts()
})

// Watch id thay đổi để load lại chi tiết sản phẩm
watch(productId, () => {
  fetchProductDetail()
  fetchRelatedProducts()
})

// Thêm computed chia nhóm sản phẩm liên quan cho carousel
const chunkedRelatedProducts = computed(() => {
  const chunkSize = 4;
  const chunks = [];
  for (let i = 0; i < relatedProducts.value.length; i += chunkSize) {
    chunks.push(relatedProducts.value.slice(i, i + chunkSize));
  }
  return chunks;
});
</script>

<style scoped>
@import 'animate.css';

* {
  box-sizing: border-box;
}

.product-detail-page {
  padding-top: 130px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.breadcrumb {
  background: none;
  padding: 0;
  margin-bottom: 0;
}

.breadcrumb-item a {
  color: #4ba27b;
  text-decoration: none;
}

.breadcrumb-item a:hover {
  color: #66ea8b;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: auto;
  min-height: 100px;
  overflow: hidden;
}

/* Product Detail Section */
.product-info {
  border-radius: 10px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  padding: 1.2rem 1rem;
}

.product-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

#productImageCarousel .carousel-item img {
  height: 500px;
  object-fit: cover;
  transition: transform 0.5s ease;
}

#productImageCarousel .carousel-item.active img {
  transform: scale(1);
}

#productImageCarousel:hover .carousel-item img {
  transform: scale(1.05);
}

.product-badge {
  z-index: 5;
}

.product-name {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
}

.product-details .color-dot {
  width: 30px !important;
  height: 30px !important;
  border-radius: 50%;
  border: 2px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
  cursor: pointer;
}

.product-details .color-dot:hover,
.product-details .color-dot.active {
  border-color: #4ba27b;
  transform: scale(1.1);
}

.product-details .btn {
  padding: 6px 12px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.product-details .btn.btn-secondary {
  background-color: #4ba27b;
  border-color: #4ba27b;
}

.product-details .btn-success {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  border: none;
}

.product-details .btn-success:hover {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  box-shadow: 0 6px 16px rgba(102, 234, 139, 0.5);
}

.product-details .btn-primary {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  border: none;
}

.product-details .btn-primary:hover {
  background: linear-gradient(135deg, #0056b3 0%, #007bff 100%);
  box-shadow: 0 6px 16px rgba(0, 123, 255, 0.5);
}

.product-details .btn:disabled {
  border: none;
  cursor: not-allowed;
  opacity: 0.6;
}

.product-details .form-control {
  height: 30px;
  border-radius: 8px;
  border: 1px solid #ddd;
  font-size: 0.9rem;
  text-align: center;
  width: 60px;
}

.product-details .form-control:focus {
  border-color: #4ba27b;
  box-shadow: 0 0 5px rgba(75, 162, 123, 0.5);
}

.product-price .current-price {
  color: #e53935 !important;
  font-size: 2rem;
}

.product-price .original-price {
  color: #aaa;
  font-size: 1.2rem;
  margin-right: 10px;
}

.product-detailed-description {
  border-top: 1px solid #eee;
  padding-top: 1.5rem;
}

/* Related Products Section */
.related-products .product-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.related-products .product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.related-products .product-image {
  height: 250px;
  overflow: hidden;
}

.related-products .product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.related-products .product-card:hover .product-image img {
  transform: scale(1.05);
}

.related-products .product-overlay {
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
}

.related-products .product-card:hover .product-overlay {
  opacity: 1;
}

.related-products .product-overlay .btn {
  padding: 8px 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  color: white;
  border: none;
}

.related-products .product-overlay .btn:hover {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  box-shadow: 0 6px 16px rgba(102, 234, 139, 0.8);
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  #productImageCarousel .carousel-item img {
    height: 400px;
  }

  .product-name {
    font-size: 1.5rem;
  }

  .product-price .current-price {
    font-size: 1.75rem;
  }

  .product-price .original-price {
    font-size: 1.25rem;
  }

  .product-details .form-control {
    width: 50px;
    height: 28px;
    font-size: 0.85rem;
  }

  .product-details .btn {
    padding: 5px 10px;
    font-size: 0.85rem;
  }

  .product-details .btn.rounded-circle {
    width: 28px;
    height: 28px;
  }

  .hero-section {
    min-height: 80px;
  }

  .breadcrumb {
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  #productImageCarousel .carousel-item img {
    height: 300px;
  }

  .product-info {
    padding: 1rem;
  }

  .product-name {
    font-size: 1.25rem;
  }

  .product-price .current-price {
    font-size: 1.5rem;
  }

  .product-price .original-price {
    font-size: 1rem;
  }

  .product-details .form-control {
    width: 45px;
    height: 26px;
    font-size: 0.8rem;
  }

  .product-details .btn {
    padding: 4px 8px;
    font-size: 0.8rem;
  }

  .product-details .btn.rounded-circle {
    width: 26px;
    height: 26px;
  }

  .related-products .product-image {
    height: 200px;
  }

  .breadcrumb {
    font-size: 0.8rem;
  }
}
.discount-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 1px;
  margin-left: 8px;
  vertical-align: middle;
}

.quantity-group {
  width: 110px;
}
.quantity-group .form-control,
.quantity-group .btn {
  height: 36px;
  min-width: 36px;
  max-width: 36px;
  padding: 0;
  font-size: 1rem;
  border-radius: 0 !important;
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ced4da;
}
.quantity-group .form-control {
  width: 38px;
  min-width: 38px;
  max-width: 38px;
  text-align: center;
  border-left: none;
  border-right: none;
}
.quantity-group .btn:first-child {
  border-top-left-radius: 18px !important;
  border-bottom-left-radius: 18px !important;
}
.quantity-group .btn:last-child {
  border-top-right-radius: 18px !important;
  border-bottom-right-radius: 18px !important;
}

/* Chỉ bỏ border khi KHÔNG disabled */
.quantity-group .btn:first-child:not(:disabled) {
  border-right: none;
}
.quantity-group .btn:last-child:not(:disabled) {
  border-left: none;
}
.quantity-group .btn:disabled {
  opacity: 1;
  filter: none;
  cursor: not-allowed;
}
.quantity-group .btn:hover,
.quantity-group .btn:focus {
  background: inherit !important;
  color: inherit !important;
  box-shadow: none !important;
  border-color: #ced4da !important;
}

/* Copy toàn bộ CSS liên quan từ TrangChu.vue vào đây để đồng bộ giao diện sản phẩm liên quan */
.product-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.3s ease;
  min-height: 300px;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
  opacity: 0.9;
}
.product-image {
  height: 250px;
  overflow: hidden;
  position: relative;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.product-card:hover .product-image img {
  transform: scale(1.05);
}
.product-overlay {
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.product-card:hover .product-overlay {
  opacity: 1;
  pointer-events: auto;
}
.product-badge {
  z-index: 5;
}
.product-info {
  padding: 1.5rem;
}
.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
}
.product-details .color-dot {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}
.product-price .current-price {
  color: #32CD32;
  font-size: 1.4rem;
  font-weight: 700;
}
.product-price .original-price {
  font-size: 1rem;
  color: #999;
}
.related-products .color-dot {
  width: 15px;
  height: 15px;
}

/* Styling cho mã sản phẩm */
.product-code {
  padding: 8px 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border-left: 4px solid #4ba27b;
}

.product-code small {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-code span {
  font-size: 1rem;
  font-family: 'Courier New', monospace;
}

/* Styling cho số lượng đã bán */
.sold-quantity-info {
  padding: 8px 12px;
  background: linear-gradient(135deg, #e8f5e8 0%, #d4edda 100%);
  border-radius: 8px;
  border: 1px solid #c3e6cb;
}

.sold-quantity-info small {
  font-size: 0.9rem;
}

.sold-quantity-info .bi-graph-up {
  color: #28a745;
}

/* Responsive cho phần số lượng và thông tin đã bán */
@media (max-width: 768px) {
  .d-flex.align-items-center.gap-3 {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 10px !important;
  }

  .sold-quantity-info {
    width: 100%;
    text-align: center;
  }

  .product-code {
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .product-code {
    padding: 6px 10px;
    font-size: 0.85rem;
  }

  .sold-quantity-info {
    padding: 6px 10px;
    font-size: 0.85rem;
  }
}
</style>