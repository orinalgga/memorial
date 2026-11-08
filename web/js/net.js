/* 추모 서버와 이야기하는 부분: REST(/api)와 STOMP 웹소켓(/ws).
   서버가 주는 모양을 화면 코드가 쓰는 모양({ at: 숫자, msg, k ... })으로 바꿔 준다. */
window.Net = (() => {
  const API = String(window.MEMORIAL_API_BASE || '').replace(/\/+$/, '');
  const WS = (API || location.origin).replace(/^http/, 'ws') + '/ws';
  const ts = x => typeof x === 'number' ? x : (Date.parse(x) || Date.now());

  const letter = d => ({ id: d.id, to: d.to, msg: d.message, name: d.name, hearts: d.hearts || 0, comments: d.comments || 0, at: ts(d.createdAt) });
  const comment = d => ({ id: d.id, letterId: d.letterId, parent: d.parentId || undefined, name: d.name, text: d.text, at: ts(d.createdAt) });
  const chat = d => ({ id: d.id, name: d.name, k: d.visitorId, text: d.text, at: ts(d.createdAt) });
  const offering = d => ({ id: d.id, kind: d.kind, name: d.name, at: ts(d.createdAt) });
  const memorial = d => ({ id: d.id, name: d.name, years: d.years || '', intro: d.intro || '', by: d.createdBy || '', at: ts(d.createdAt),
    photo: d.hasPhoto ? `${API}/api/memorials/${d.id}/photo?v=${ts(d.createdAt)}` : '' });

  async function call(method, path, body, headers) {
    let r;
    try {
      r = await fetch(API + path, { method, headers: { ...(body ? { 'Content-Type': 'application/json' } : {}), ...(headers || {}) },
        body: body ? JSON.stringify(body) : undefined });
    } catch (e) { throw new Error('서버에 닿지 못했어요. 잠시 뒤 다시 시도해 주세요.'); }
    if (r.status === 204) return null;
    const json = await r.json().catch(() => null);
    if (!r.ok) { const e = new Error((json && json.message) || '잠시 뒤 다시 시도해 주세요.'); e.status = r.status; throw e; }
    return json;
  }
  const sp = s => encodeURIComponent(s);

  /* ---- live: one STOMP connection, subscriptions survive reconnects ---- */
  const subs = new Set(), connectHooks = new Set();
  let client = null, connected = false;
  function attach(s) { if (connected && !s.sub) s.sub = client.subscribe(s.dest, msg => { let b; try { b = JSON.parse(msg.body); } catch (e) { return; } s.cb(b); }); }
  function listen(dest, cb) {
    const s = { dest, cb, sub: null }; subs.add(s); attach(s);
    return () => { subs.delete(s); if (s.sub) { try { s.sub.unsubscribe(); } catch (e) {} } };
  }
  function start() {
    if (client || !window.StompJs) return;
    client = new StompJs.Client({ brokerURL: WS, reconnectDelay: 3000, heartbeatIncoming: 25000, heartbeatOutgoing: 25000 });
    client.onConnect = () => { connected = true; subs.forEach(s => { s.sub = null; attach(s); }); connectHooks.forEach(f => { try { f(); } catch (e) {} }); };
    client.onWebSocketClose = () => { connected = false; subs.forEach(s => { s.sub = null; }); };
    client.activate();
  }

  return {
    api: API,
    start,
    onConnect(f) { connectHooks.add(f); },
    /* memorials */
    memorials: async () => (await call('GET', '/api/memorials')).map(memorial),
    async createMemorial(m) {
      const r = await call('POST', '/api/memorials', { name: m.name, years: m.years, intro: m.intro, photo: m.photo || null, createdBy: m.by });
      return { memorial: memorial(r.memorial), editToken: r.editToken };
    },
    onMemorial: cb => listen('/topic/memorials', d => cb(memorial(d))),
    /* stories, empathy, comments */
    letters: async space => (await call('GET', `/api/spaces/${sp(space)}/letters`)).map(letter),
    addLetter: async (space, l) => letter(await call('POST', `/api/spaces/${sp(space)}/letters`, l)),
    heart: (id, visitorId) => call('POST', `/api/letters/${sp(id)}/hearts`, { visitorId }),
    comments: async id => (await call('GET', `/api/letters/${sp(id)}/comments`)).map(comment),
    addComment: async (id, c) => comment(await call('POST', `/api/letters/${sp(id)}/comments`, c)),
    /* candles and flowers */
    offerings: async space => (await call('GET', `/api/spaces/${sp(space)}/offerings`)).items.map(offering),
    addOffering: async (space, o) => offering(await call('POST', `/api/spaces/${sp(space)}/offerings`, o)),
    /* chat */
    chat: async space => (await call('GET', `/api/spaces/${sp(space)}/chat`)).map(chat),
    say: async (space, m) => chat(await call('POST', `/api/spaces/${sp(space)}/chat`, m)),
    /* avatar faces: returned as data URLs so they can be drawn into WebGL textures */
    putFace: (visitorId, image) => call('PUT', `/api/avatars/${sp(visitorId)}/face`, { image }),
    deleteFace: visitorId => call('DELETE', `/api/avatars/${sp(visitorId)}/face`),
    async face(visitorId) {
      const r = await fetch(`${API}/api/avatars/${sp(visitorId)}/face`).catch(() => null);
      if (!r || !r.ok) return null;
      const blob = await r.blob();
      return await new Promise(res => { const fr = new FileReader(); fr.onload = () => res(fr.result); fr.onerror = () => res(null); fr.readAsDataURL(blob); });
    },
    /* one space at a time: events (letter/heart/comment/offering/chat) and who is here */
    watchSpace(space, onEvent, onPresence, onPeers) {
      const offs = [
        listen(`/topic/spaces/${space}/events`, e => {
          const d = e.data;
          if (e.type === 'letter') onEvent('letter', letter(d));
          else if (e.type === 'heart') onEvent('heart', d);
          else if (e.type === 'comment') onEvent('comment', { comment: comment(d.comment), comments: d.comments });
          else if (e.type === 'offering') onEvent('offering', offering(d));
          else if (e.type === 'chat') onEvent('chat', chat(d));
        }),
        listen(`/topic/spaces/${space}/presence`, onPresence),
        listen(`/app/spaces/${space}/peers`, onPeers),
      ];
      return () => offs.forEach(f => f());
    },
    onVisitors: cb => listen('/topic/visitors', cb),
    visitors: cb => listen('/app/visitors', cb),
    presence(space, state) {
      if (!connected) return false;
      client.publish({ destination: `/app/spaces/${space}/presence`, body: JSON.stringify(state) });
      return true;
    },
  };
})();
