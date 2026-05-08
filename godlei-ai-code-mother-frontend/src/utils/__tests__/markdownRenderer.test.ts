import { describe, expect, it } from 'vitest'
import { renderChatMarkdown } from '../markdownRenderer'

describe('renderChatMarkdown', () => {
  it('renders markdown headings and paragraphs into html', () => {
    const html = renderChatMarkdown('## 我的博客\n\n这是首页介绍。')

    expect(html).toContain('<h2>我的博客</h2>')
    expect(html).toContain('<p>这是首页介绍。</p>')
  })

  it('keeps inline html content available for richer ai replies', () => {
    const html = renderChatMarkdown('<div class="hero">preview</div>')

    expect(html).toContain('<div class="hero">preview</div>')
  })

  it('highlights html css and javascript code fences', () => {
    const html = renderChatMarkdown(
      ['```html', '<section class="hero">Hello</section>', '```', '', '```css', '.hero { color: #155eef; }', '```', '', '```javascript', 'const answer = 42', '```'].join('\n'),
    )

    expect(html).toContain('language-html')
    expect(html).toContain('language-css')
    expect(html).toContain('language-javascript')
    expect(html).toContain('hljs')
  })

  it('falls back to escaped code blocks when the language is unknown', () => {
    const html = renderChatMarkdown(['```custom-lang', '<unsafe>', '```'].join('\n'))

    expect(html).toContain('&lt;unsafe&gt;')
    expect(html).toContain('<pre class="chat-code-block"><code class="hljs">')
  })
})
