import { describe, expect, it } from 'vitest'
import { consumeSseChunk, createSseAccumulator, flushSseAccumulator } from '../appStream'

describe('consumeSseChunk', () => {
  it('parses complete events from one chunk', () => {
    const accumulator = createSseAccumulator()

    expect(consumeSseChunk(accumulator, 'data: hello\n\ndata: world\n\n')).toEqual([
      'hello',
      'world',
    ])
  })

  it('combines multi-line data into one event body', () => {
    const accumulator = createSseAccumulator()

    expect(consumeSseChunk(accumulator, 'data: line 1\ndata: line 2\n\n')).toEqual([
      'line 1\nline 2',
    ])
  })

  it('waits for incomplete chunks before emitting an event', () => {
    const accumulator = createSseAccumulator()

    expect(consumeSseChunk(accumulator, 'data: hello')).toEqual([])
    expect(consumeSseChunk(accumulator, ' world\n\n')).toEqual(['hello world'])
  })
})

describe('flushSseAccumulator', () => {
  it('emits the final buffered event when the stream closes without a blank line', () => {
    const accumulator = createSseAccumulator()

    expect(consumeSseChunk(accumulator, 'data: final message')).toEqual([])
    expect(flushSseAccumulator(accumulator)).toEqual(['final message'])
  })
})
