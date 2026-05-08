import { describe, expect, it } from 'vitest'
import {
  HOME_HERO_SUBTITLE,
  HOME_HERO_TITLE,
  HOME_PROMPT_SUGGESTIONS,
} from '../homeContent'

describe('homeContent', () => {
  it('uses the updated centered hero copy', () => {
    expect(HOME_HERO_TITLE).toBe('AI 应用生成平台')
    expect(HOME_HERO_SUBTITLE).toBe('一句话轻松创建网站应用')
  })

  it('provides four concise website generation prompts', () => {
    expect(HOME_PROMPT_SUGGESTIONS).toHaveLength(4)
    expect(HOME_PROMPT_SUGGESTIONS.every((item) => item.length <= 50)).toBe(true)
  })
})
