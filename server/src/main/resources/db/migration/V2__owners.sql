-- 로그인이 없으므로 글쓴이 확인은 브라우저마다 만든 비밀 키로 한다. DB에는 그 키의 SHA-256만 둔다.
alter table letters add column owner_hash varchar(64);
alter table comments add column owner_hash varchar(64);
alter table chat_messages add column owner_hash varchar(64);
