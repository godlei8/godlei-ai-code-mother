import { describe, expect, it } from 'vitest'
import { parseApiJson } from '../jsonParser'

describe('parseApiJson', () => {
  it('preserves long ids as strings instead of unsafe numbers', () => {
    const payload = parseApiJson('{"code":0,"data":{"id":410281457040470016,"userId":410281457040470017}}')

    expect(payload).toEqual({
      code: 0,
      data: {
        id: '410281457040470016',
        userId: '410281457040470017',
      },
    })
  })

  it('keeps regular small numbers as numbers', () => {
    const payload = parseApiJson('{"code":0,"data":{"pageSize":20,"totalRow":3}}')

    expect(payload).toEqual({
      code: 0,
      data: {
        pageSize: 20,
        totalRow: 3,
      },
    })
  })
})
