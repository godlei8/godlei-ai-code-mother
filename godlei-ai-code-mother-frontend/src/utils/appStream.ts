export interface SseAccumulator {
  buffer: string
  eventDataLines: string[]
}

export const createSseAccumulator = (): SseAccumulator => ({
  buffer: '',
  eventDataLines: [],
})

const normalizeEventPayload = (value: string) => {
  const trimmedValue = value.trim()
  if (!trimmedValue.startsWith('{')) {
    return value
  }

  try {
    const parsed = JSON.parse(trimmedValue) as { d?: unknown }
    if (typeof parsed?.d === 'string') {
      return parsed.d
    }
    if (typeof parsed?.d === 'number') {
      return String(parsed.d)
    }
  } catch {
    return value
  }

  return value
}

const flushEventDataLines = (accumulator: SseAccumulator) => {
  if (accumulator.eventDataLines.length === 0) {
    return ''
  }

  const value = normalizeEventPayload(accumulator.eventDataLines.join('\n'))
  accumulator.eventDataLines = []
  return value
}

export const consumeSseChunk = (accumulator: SseAccumulator, chunk: string) => {
  accumulator.buffer += chunk
  const events: string[] = []

  while (true) {
    const lineBreakIndex = accumulator.buffer.indexOf('\n')
    if (lineBreakIndex < 0) {
      break
    }

    const rawLine = accumulator.buffer.slice(0, lineBreakIndex)
    accumulator.buffer = accumulator.buffer.slice(lineBreakIndex + 1)

    const normalizedLine = rawLine.endsWith('\r') ? rawLine.slice(0, -1) : rawLine

    if (!normalizedLine) {
      const eventValue = flushEventDataLines(accumulator)
      if (eventValue) {
        events.push(eventValue)
      }
      continue
    }

    if (normalizedLine.startsWith('data:')) {
      accumulator.eventDataLines.push(normalizedLine.slice(5).trimStart())
    }
  }

  return events
}

export const flushSseAccumulator = (accumulator: SseAccumulator) => {
  if (accumulator.buffer) {
    const trailingLine = accumulator.buffer.endsWith('\r')
      ? accumulator.buffer.slice(0, -1)
      : accumulator.buffer

    if (trailingLine.startsWith('data:')) {
      accumulator.eventDataLines.push(trailingLine.slice(5).trimStart())
    }

    accumulator.buffer = ''
  }

  const finalEvent = flushEventDataLines(accumulator)
  return finalEvent ? [finalEvent] : []
}
