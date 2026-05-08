import JSONBigFactory from 'json-bigint'

const jsonBig = JSONBigFactory({
  storeAsString: true,
})

export const parseApiJson = (raw: unknown) => {
  if (typeof raw !== 'string') {
    return raw
  }

  const trimmed = raw.trim()
  if (!trimmed) {
    return raw
  }

  if (!trimmed.startsWith('{') && !trimmed.startsWith('[')) {
    return raw
  }

  try {
    return jsonBig.parse(trimmed)
  } catch {
    return raw
  }
}
