import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const escapeHtml = (code: string) =>
  code
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

const normalizeLanguage = (language: string) => {
  const normalized = language.trim().toLowerCase()

  if (normalized === 'js') {
    return 'javascript'
  }

  if (normalized === 'ts') {
    return 'typescript'
  }

  return normalized
}

const markdown = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
  highlight(code: string, language: string) {
    const normalizedLanguage = normalizeLanguage(language)

    if (normalizedLanguage && hljs.getLanguage(normalizedLanguage)) {
      const highlightedCode = hljs.highlight(code, {
        language: normalizedLanguage,
        ignoreIllegals: true,
      }).value

      return `<pre class="chat-code-block"><code class="hljs language-${normalizedLanguage}">${highlightedCode}</code></pre>`
    }

    return `<pre class="chat-code-block"><code class="hljs">${escapeHtml(code)}</code></pre>`
  },
})

export const renderChatMarkdown = (content: string) => markdown.render(content)
