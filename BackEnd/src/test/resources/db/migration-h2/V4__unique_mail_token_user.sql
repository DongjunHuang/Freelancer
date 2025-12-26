ALTER TABLE mail_tokens
  ADD CONSTRAINT uniq_mail_tokens_user UNIQUE (user_id);