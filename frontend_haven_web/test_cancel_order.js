// Test script cho chức năng hủy đơn hàng
// Chạy trong browser console tại http://localhost:3001/tracking

console.log('🧪 Bắt đầu test chức năng hủy đơn hàng...');

// Test 1: Kiểm tra button "Yêu cầu hủy" có hiển thị không
function testCancelButtonVisibility() {
  console.log('\n📋 Test 1: Kiểm tra button "Yêu cầu hủy"');
  
  const cancelButtons = document.querySelectorAll('.btn-danger');
  console.log(`✅ Tìm thấy ${cancelButtons.length} button hủy đơn hàng`);
  
  cancelButtons.forEach((btn, index) => {
    const text = btn.textContent.trim();
    console.log(`   Button ${index + 1}: "${text}"`);
  });
  
  return cancelButtons.length > 0;
}

// Test 2: Kiểm tra modal hủy đơn hàng
function testCancelModal() {
  console.log('\n📋 Test 2: Kiểm tra modal hủy đơn hàng');
  
  // Click vào button hủy đầu tiên
  const firstCancelButton = document.querySelector('.btn-danger');
  if (firstCancelButton) {
    console.log('🖱️ Click vào button hủy đơn hàng...');
    firstCancelButton.click();
    
    // Đợi modal hiển thị
    setTimeout(() => {
      const modal = document.querySelector('.modal.show');
      if (modal) {
        console.log('✅ Modal hủy đơn hàng hiển thị thành công');
        
        // Kiểm tra các elements trong modal
        const modalTitle = modal.querySelector('.modal-title');
        const textarea = modal.querySelector('textarea');
        const submitButton = modal.querySelector('.btn-danger');
        
        console.log(`   - Tiêu đề: ${modalTitle?.textContent}`);
        console.log(`   - Textarea: ${textarea ? 'Có' : 'Không'}`);
        console.log(`   - Button submit: ${submitButton ? 'Có' : 'Không'}`);
        
        // Test validation
        testModalValidation(modal);
        
      } else {
        console.log('❌ Modal không hiển thị');
      }
    }, 500);
  } else {
    console.log('❌ Không tìm thấy button hủy đơn hàng');
  }
}

// Test 3: Kiểm tra validation trong modal
function testModalValidation(modal) {
  console.log('\n📋 Test 3: Kiểm tra validation');
  
  const textarea = modal.querySelector('textarea');
  const submitButton = modal.querySelector('.btn-danger');
  
  if (textarea && submitButton) {
    // Test 1: Submit với textarea rỗng
    console.log('🧪 Test submit với textarea rỗng...');
    textarea.value = '';
    submitButton.click();
    
    setTimeout(() => {
      const errorMessage = modal.querySelector('.invalid-feedback');
      if (errorMessage) {
        console.log(`✅ Validation hoạt động: ${errorMessage.textContent}`);
      } else {
        console.log('❌ Không có thông báo lỗi validation');
      }
      
      // Test 2: Submit với text quá ngắn
      console.log('🧪 Test submit với text quá ngắn...');
      textarea.value = 'Ngắn';
      submitButton.click();
      
      setTimeout(() => {
        const errorMessage2 = modal.querySelector('.invalid-feedback');
        if (errorMessage2) {
          console.log(`✅ Validation độ dài: ${errorMessage2.textContent}`);
        }
        
        // Test 3: Submit với text hợp lệ
        console.log('🧪 Test submit với text hợp lệ...');
        textarea.value = 'Tôi muốn hủy đơn hàng vì đã mua ở nơi khác rẻ hơn và thuận tiện hơn';
        submitButton.click();
        
        // Đóng modal sau khi test xong
        setTimeout(() => {
          const closeButton = modal.querySelector('.btn-close');
          if (closeButton) {
            closeButton.click();
            console.log('✅ Đã đóng modal');
          }
        }, 2000);
        
      }, 1000);
      
    }, 1000);
  }
}

// Test 4: Kiểm tra API call
async function testCancelAPI() {
  console.log('\n📋 Test 4: Kiểm tra API call');
  
  const token = localStorage.getItem('authToken') || localStorage.getItem('token');
  if (!token) {
    console.log('❌ Không có token, vui lòng đăng nhập trước');
    return;
  }
  
  // Lấy danh sách hóa đơn
  try {
    const response = await fetch('http://localhost:8080/khach-hang/hoa-don/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.ok) {
      const data = await response.json();
      const hoaDons = data.content || [];
      
      if (hoaDons.length > 0) {
        const cancelableOrder = hoaDons.find(hd => hd.trangThai === 0 || hd.trangThai === 1);
        
        if (cancelableOrder) {
          console.log(`✅ Tìm thấy đơn hàng có thể hủy: ${cancelableOrder.maHoaDon}`);
          
          // Test API hủy đơn hàng
          const cancelResponse = await fetch(`http://localhost:8080/khach-hang/hoa-don/huy/${cancelableOrder.id}`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              lyDo: 'Test hủy đơn hàng từ script test - Tôi muốn hủy đơn hàng vì đã mua ở nơi khác rẻ hơn'
            })
          });
          
          console.log(`📊 API Response Status: ${cancelResponse.status}`);
          
          if (cancelResponse.ok) {
            const cancelData = await cancelResponse.json();
            console.log('✅ API hủy đơn hàng thành công:', cancelData);
          } else {
            const errorData = await cancelResponse.text();
            console.log('❌ API hủy đơn hàng thất bại:', errorData);
          }
          
        } else {
          console.log('⚠️ Không có đơn hàng nào có thể hủy (trạng thái 0 hoặc 1)');
        }
      } else {
        console.log('⚠️ Không có đơn hàng nào');
      }
    } else {
      console.log('❌ Không thể lấy danh sách hóa đơn');
    }
  } catch (error) {
    console.error('❌ Lỗi khi test API:', error);
  }
}

// Test 5: Kiểm tra responsive design
function testResponsiveDesign() {
  console.log('\n📋 Test 5: Kiểm tra responsive design');
  
  const buttons = document.querySelectorAll('.btn-danger');
  buttons.forEach((btn, index) => {
    const parent = btn.closest('.d-flex');
    if (parent) {
      const classes = parent.className;
      console.log(`   Button ${index + 1} container classes: ${classes}`);
      
      if (classes.includes('flex-column') && classes.includes('flex-sm-row')) {
        console.log('✅ Responsive classes đúng');
      } else {
        console.log('❌ Responsive classes thiếu');
      }
    }
  });
}

// Test 6: Kiểm tra accessibility
function testAccessibility() {
  console.log('\n📋 Test 6: Kiểm tra accessibility');
  
  // Kiểm tra labels
  const textarea = document.querySelector('textarea');
  if (textarea) {
    const label = document.querySelector('label[for="lyDoHuy"]');
    if (label) {
      console.log('✅ Textarea có label đúng');
    } else {
      console.log('❌ Textarea thiếu label');
    }
  }
  
  // Kiểm tra ARIA attributes
  const modal = document.querySelector('.modal');
  if (modal) {
    const hasTabindex = modal.hasAttribute('tabindex');
    console.log(`   Modal tabindex: ${hasTabindex ? 'Có' : 'Không'}`);
  }
  
  // Kiểm tra keyboard navigation
  const closeButtons = document.querySelectorAll('.btn-close');
  console.log(`   Số button đóng: ${closeButtons.length}`);
}

// Chạy tất cả tests
async function runAllTests() {
  console.log('🚀 Bắt đầu chạy tất cả tests...\n');
  
  // Test UI
  const hasCancelButtons = testCancelButtonVisibility();
  
  if (hasCancelButtons) {
    testCancelModal();
    testResponsiveDesign();
    testAccessibility();
  }
  
  // Test API (chạy sau để không ảnh hưởng UI)
  setTimeout(() => {
    testCancelAPI();
  }, 3000);
  
  console.log('\n✅ Hoàn thành tất cả tests!');
}

// Export functions để có thể gọi riêng lẻ
window.testCancelOrder = {
  testCancelButtonVisibility,
  testCancelModal,
  testCancelAPI,
  testResponsiveDesign,
  testAccessibility,
  runAllTests
};

// Tự động chạy tests khi load script
runAllTests(); 