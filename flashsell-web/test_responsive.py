"""
测试 FlashSell 响应式设计
验证移动端、平板、桌面端的显示效果
"""
from playwright.sync_api import sync_playwright
import time

# 常见设备断点
DEVICE_SIZES = {
    'iPhone SE': {'width': 375, 'height': 667},
    'iPhone 12': {'width': 390, 'height': 844},
    'iPad': {'width': 768, 'height': 1024},
    'iPad Pro': {'width': 1024, 'height': 1366},
    'Desktop': {'width': 1440, 'height': 900},
    'Large Desktop': {'width': 1920, 'height': 1080},
}

PAGES_TO_TEST = [
    {'url': '/', 'name': 'Home'},
    {'url': '/profile', 'name': 'Profile'},
    {'url': '/market', 'name': 'Market'},
    {'url': '/subscription', 'name': 'Subscription'},
    {'url': '/hot-products', 'name': 'HotProducts'},
    {'url': '/favorites', 'name': 'Favorites'},
]

def test_responsive_design():
    """测试响应式设计"""
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        page = browser.new_page()

        print("\n📱 开始测试响应式设计...")
        print("=" * 70)

        # 首先登录
        print("\n🔐 执行登录...")
        page.goto('http://localhost:3001/login')
        page.wait_for_load_state('networkidle')

        # 填写登录信息
        page.fill('input[type="tel"]', '13800138000')
        page.fill('input[placeholder*="验证码"]', '123456')
        page.click('button:has-text("登录")')

        # 等待登录完成
        try:
            page.wait_for_url('**/', timeout=5000)
            print("  ✅ 登录成功")
        except:
            print("  ⚠️  登录可能失败，继续测试...")

        timestamp = time.strftime('%Y%m%d_%H%M%S')

        # 测试每个设备尺寸
        for device_name, size in DEVICE_SIZES.items():
            print(f"\n{'='*70}")
            print(f"📱 测试设备: {device_name} ({size['width']}x{size['height']})")
            print('='*70)

            # 设置视口大小
            page.set_viewport_size(size)
            page.wait_for_timeout(500)

            # 测试每个页面
            for page_info in PAGES_TO_TEST:
                print(f"\n  测试页面: {page_info['name']}")

                # 导航到页面
                page.goto(f'http://localhost:3001{page_info["url"]}')
                page.wait_for_load_state('networkidle')
                page.wait_for_timeout(1000)

                # 检查横向滚动（不应该有）
                scroll_width = page.evaluate('document.body.scrollWidth')
                viewport_width = size['width']

                if scroll_width > viewport_width + 10:  # 允许10px误差
                    print(f"    ❌ 发现横向滚动! scrollWidth={scroll_width}, viewport={viewport_width}")
                else:
                    print(f"    ✅ 无横向滚动 ({scroll_width}px)")

                # 检查关键元素
                # 检查导航栏是否可见
                sidebar = page.locator('aside[role="navigation"]')
                if sidebar.is_visible():
                    # 在小屏幕上导航栏应该是可折叠的
                    if size['width'] < 768:
                        print(f"    ✅ 移动端导航栏可见（可能是侧边栏或汉堡菜单）")
                    else:
                        print(f"    ✅ 桌面端导航栏可见")

                # 检查主要内容区域
                main_content = page.locator('main')
                if main_content.is_visible():
                    main_width = main_content.evaluate('el => el.offsetWidth')
                    print(f"    ✅ 主内容区域可见 (宽度: {main_width}px)")

                # 检查卡片布局
                cards = page.locator('.glass-card')
                card_count = cards.count()
                if card_count > 0:
                    first_card = cards.first
                    card_width = first_card.evaluate('el => el.offsetWidth')
                    print(f"    ✅ 找到 {card_count} 个卡片，卡片宽度约 {card_width}px")

                # 检查按钮是否可点击（触摸目标 ≥ 44x44）
                if size['width'] <= 480:  # 移动端
                    buttons = page.locator('button').all()
                    small_buttons = 0
                    for btn in buttons[:5]:  # 检查前5个按钮
                        box = btn.bounding_box()
                        if box:
                            width = box['width'] or 0
                            height = box['height'] or 0
                            if width < 44 or height < 44:
                                small_buttons += 1

                    if small_buttons == 0:
                        print(f"    ✅ 移动端按钮触摸目标符合标准 (≥44x44)")
                    else:
                        print(f"    ⚠️  发现 {small_buttons} 个小按钮 (<44x44)")

                # 截图
                screenshot_name = f"responsive_{device_name.replace(' ', '_')}_{page_info['name']}_{timestamp}.png"
                page.screenshot(path=screenshot_name, full_page=True)
                print(f"    📸 截图已保存: {screenshot_name}")

        browser.close()

        print("\n" + "="*70)
        print("✅ 响应式设计测试完成！")
        print("\n📋 测试总结:")
        print("  • 测试设备数:", len(DEVICE_SIZES))
        print("  • 测试页面数:", len(PAGES_TO_TEST))
        print("  • 总截图数:", len(DEVICE_SIZES) * len(PAGES_TO_TEST))
        print("\n🎯 响应式断点覆盖:")
        print("  • 移动端: 375px, 390px")
        print("  • 平板: 768px, 1024px")
        print("  • 桌面: 1440px, 1920px")
        print("\n✨ 所有截图已保存，可查看各设备上的显示效果！")

if __name__ == '__main__':
    test_responsive_design()
