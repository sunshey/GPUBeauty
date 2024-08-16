import { createRequire } from 'module'
import { defineConfig, type DefaultTheme } from 'vitepress'

const require = createRequire(import.meta.url)

export const zh = defineConfig({
  lang: 'zh-Hans',
  description: 'Cross-Platform AI Beauty Effects Library, Achieving Commercial-Grade Beauty Effects. Written in C++11, Based on OpenGL/ES and VNN.',

  themeConfig: {
    nav: nav(),

    sidebar: {
      '/zh/guide/': { base: '/zh/guide/', items: sidebarGuide() },
      '/zh/reference/': { base: '/zh/reference/', items: sidebarReference() }
    },

    editLink: {
      pattern: 'https://github.com/pixpark/gpupixel/edit/main/docs/docs/:path',
      text: '在 GitHub 上编辑此页面'
    },

    footer: {
      message: '基于 MIT 许可发布',
      copyright: `版权所有 © 2019-${new Date().getFullYear()} PixPark`
    },

    docFooter: {
      prev: '上一页',
      next: '下一页'
    },

    outline: {
      label: '文章内容'
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'medium'
      }
    },

    langMenuLabel: '多语言',
    returnToTopLabel: '回到顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式'
  }
})

function nav(): DefaultTheme.NavItem[] {
  return [
    {
      text: '指南',
      link: '/zh/guide/intro',
      activeMatch: '/zh/guide/'
    },
    {
      text: '参考',    
      link: '/zh/reference/filter-list',
      activeMatch: '/zh/reference/'
    },
    {
      text: '1.2.5',
      items: [
        {
          text: '更新日志',
          link: 'https://github.com/pixpark/gpupixel/blob/main/CHANGELOG.md'
        },
        {
          text: '参与贡献',
          link: 'https://github.com/pixpark/gpupixel/issues/new/choose'
        }
      ]
    },
    {
      text: 'PixPark',
      link: 'https://pixpark.net',
    },
  ]
}

function sidebarGuide(): DefaultTheme.SidebarItem[] {
  return [
    {
      text: '快速开始',
      collapsed: false,
      items: [
        { text: '简介', link: 'intro' },
        { text: '编译', link: 'build' },
        { text: '示例', link: 'demo' },
        { text: '集成', link: 'integrated' },
      ]
    },
    {
      text: '接口调用',
      collapsed: false,
      items: [
        { text: '基础滤镜', link: '#' },
        { text: '美妆特效', link: '#' },
        { text: '输入输出', link: '#' },
      ]
    },
    {
      text: '自定义',
      collapsed: false,
      items: [
        { text: '自定义滤镜', link: '#' },
        { text: '自定义输入', link: '#' },
        { text: '自定义输出', link: '#' },
      ]
    },
    {
      text: '其他',
      collapsed: false,
      items: [
        { text: '架构分析', link: '#' },
        { text: '代码调试', link: '#' },
      ]
    },
    { text: '配置和 API 参考', base: '/zh/reference/', link: 'filter-list' }
  ]
}

function sidebarReference(): DefaultTheme.SidebarItem[] {
  return [
    {
      text: '参考',
      items: [
        { text: '滤镜列表', link: 'filter-list' },
      ]
    }
  ]
}

export const search = {
    zh: { // make this `root` if you want to translate the default locale
      translations: {
        button: {
          buttonText: '搜索',
          buttonAriaLabel: '搜索'
        },
        modal: {
          displayDetails: '显示详细列表',
          resetButtonTitle: '重置搜索',
          backButtonTitle: '关闭搜索',
          noResultsText: '没有结果',
          footer: {
            selectText: '选择',
            selectKeyAriaLabel: '输入',
            navigateText: '导航',
            navigateUpKeyAriaLabel: '上箭头',
            navigateDownKeyAriaLabel: '下箭头',
            closeText: '关闭',
            closeKeyAriaLabel: 'esc'
          }
        }
      }
    }
}
