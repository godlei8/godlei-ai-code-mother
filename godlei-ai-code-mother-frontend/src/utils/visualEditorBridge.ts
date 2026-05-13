export type VisualSelectedElementInfo = {
  tagName: string
  id?: string
  className?: string
  textSnippet?: string
  selector: string
}

type VisualEditorMessage =
  | {
      source: 'godlei-visual-editor'
      channel: string
      type: 'select'
      payload: VisualSelectedElementInfo | null
    }
  | {
      source: 'godlei-visual-editor'
      channel: string
      type: 'mode'
      payload: { active: boolean }
    }

type CreateVisualEditorBridgeOptions = {
  iframe: HTMLIFrameElement
  onSelect: (payload: VisualSelectedElementInfo | null) => void
}

export type VisualEditorBridge = {
  start: () => boolean
  stop: () => boolean
  clearSelection: () => boolean
  dispose: () => void
}

const BRIDGE_SOURCE = 'godlei-visual-editor'
const BRIDGE_KEY = '__godleiVisualEditorBridge__'

const createChannel = () => `visual-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`

const createInjectorScript = (channel: string) => `
(function () {
  var bridgeKey = '${BRIDGE_KEY}';
  var styleId = 'godlei-visual-editor-style';

  function ensureStyle(doc) {
    if (doc.getElementById(styleId)) {
      return;
    }
    var style = doc.createElement('style');
    style.id = styleId;
    style.textContent =
      '.godlei-visual-hover-outline{outline:2px solid rgba(59,130,246,0.75)!important;outline-offset:-1px!important;cursor:pointer!important;}' +
      '.godlei-visual-selected-outline{outline:2px solid rgba(29,78,216,0.95)!important;outline-offset:-1px!important;}';
    doc.head.appendChild(style);
  }

  function createSelector(el) {
    if (!el || !el.tagName) {
      return '';
    }
    if (el.id) {
      return '#' + el.id;
    }
    var segments = [];
    var current = el;
    while (current && current.nodeType === 1 && segments.length < 6) {
      var tag = current.tagName.toLowerCase();
      var className = '';
      if (current.classList && current.classList.length) {
        className = '.' + Array.prototype.slice.call(current.classList).slice(0, 2).join('.');
      }
      var index = 1;
      var sibling = current.previousElementSibling;
      while (sibling) {
        if (sibling.tagName === current.tagName) {
          index += 1;
        }
        sibling = sibling.previousElementSibling;
      }
      var nth = index > 1 ? ':nth-of-type(' + index + ')' : '';
      segments.unshift(tag + className + nth);
      current = current.parentElement;
    }
    return segments.join(' > ');
  }

  function getElementInfo(el) {
    var text = (el.innerText || el.textContent || '').trim().replace(/\\s+/g, ' ');
    return {
      tagName: (el.tagName || '').toLowerCase(),
      id: el.id || undefined,
      className: (el.className && String(el.className).trim()) || undefined,
      textSnippet: text ? text.slice(0, 120) : undefined,
      selector: createSelector(el)
    };
  }

  function post(type, payload) {
    window.parent.postMessage(
      {
        source: '${BRIDGE_SOURCE}',
        channel: state.channel,
        type: type,
        payload: payload
      },
      window.location.origin
    );
  }

  function clearHover() {
    if (state.hovered && state.hovered.classList) {
      state.hovered.classList.remove('godlei-visual-hover-outline');
    }
    state.hovered = null;
  }

  function clearSelected() {
    if (state.selected && state.selected.classList) {
      state.selected.classList.remove('godlei-visual-selected-outline');
    }
    state.selected = null;
  }

  function isIgnoredTarget(target) {
    if (!target || !target.tagName) {
      return true;
    }
    if (target.id === styleId) {
      return true;
    }
    return false;
  }

  function handlePointerMove(event) {
    if (!state.active) {
      return;
    }
    var target = event.target;
    if (!target || !(target instanceof Element) || isIgnoredTarget(target)) {
      clearHover();
      return;
    }
    if (target === state.selected) {
      clearHover();
      return;
    }
    clearHover();
    target.classList.add('godlei-visual-hover-outline');
    state.hovered = target;
  }

  function handlePointerLeave() {
    if (!state.active) {
      return;
    }
    clearHover();
  }

  function handleClick(event) {
    if (!state.active) {
      return;
    }
    var target = event.target;
    if (!target || !(target instanceof Element) || isIgnoredTarget(target)) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();
    clearHover();
    clearSelected();
    target.classList.add('godlei-visual-selected-outline');
    state.selected = target;
    post('select', getElementInfo(target));
  }

  function start() {
    if (state.active) {
      return;
    }
    state.active = true;
    ensureStyle(document);
    document.addEventListener('mousemove', handlePointerMove, true);
    document.addEventListener('mouseleave', handlePointerLeave, true);
    document.addEventListener('click', handleClick, true);
    post('mode', { active: true });
  }

  function stop() {
    if (!state.active) {
      return;
    }
    state.active = false;
    document.removeEventListener('mousemove', handlePointerMove, true);
    document.removeEventListener('mouseleave', handlePointerLeave, true);
    document.removeEventListener('click', handleClick, true);
    clearHover();
    post('mode', { active: false });
  }

  var bridge = window[bridgeKey];
  if (!bridge || typeof bridge !== 'object') {
    var state = {
      active: false,
      selected: null,
      hovered: null,
      channel: '${channel}'
    };

    bridge = {
      start: start,
      stop: stop,
      clearSelection: function () {
        clearSelected();
        post('select', null);
      },
      setChannel: function (value) {
        state.channel = value || state.channel;
      }
    };

    window[bridgeKey] = bridge;
    return;
  }

  if (typeof bridge.setChannel === 'function') {
    bridge.setChannel('${channel}');
  }
})();
`

const injectBridge = (iframe: HTMLIFrameElement, channel: string) => {
  const doc = iframe.contentDocument
  const win = iframe.contentWindow as (Window & Record<string, unknown>) | null
  if (!doc || !win) {
    return null
  }
  const script = doc.createElement('script')
  script.type = 'text/javascript'
  script.text = createInjectorScript(channel)
  doc.documentElement.appendChild(script)
  script.remove()
  return win[BRIDGE_KEY] as
    | {
        start?: () => void
        stop?: () => void
        clearSelection?: () => void
      }
    | null
}

const callBridgeMethod = (
  iframe: HTMLIFrameElement,
  channel: string,
  method: 'start' | 'stop' | 'clearSelection',
) => {
  const bridge = injectBridge(iframe, channel)
  if (!bridge) {
    return false
  }
  const fn = bridge[method]
  if (typeof fn !== 'function') {
    return false
  }
  fn()
  return true
}

export const createVisualEditorBridge = (
  options: CreateVisualEditorBridgeOptions,
): VisualEditorBridge => {
  const channel = createChannel()
  const { iframe, onSelect } = options

  const handleMessage = (event: MessageEvent) => {
    if (event.origin !== window.location.origin || event.source !== iframe.contentWindow) {
      return
    }

    const data = event.data as VisualEditorMessage | undefined
    if (!data || data.source !== BRIDGE_SOURCE || data.channel !== channel) {
      return
    }

    if (data.type === 'select') {
      onSelect(data.payload ?? null)
    }
  }

  window.addEventListener('message', handleMessage)

  return {
    start: () => callBridgeMethod(iframe, channel, 'start'),
    stop: () => callBridgeMethod(iframe, channel, 'stop'),
    clearSelection: () => callBridgeMethod(iframe, channel, 'clearSelection'),
    dispose: () => {
      window.removeEventListener('message', handleMessage)
      callBridgeMethod(iframe, channel, 'stop')
    },
  }
}
