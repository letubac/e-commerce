SELECT * FROM users 
WHERE (username = /*username*/ OR email = /*email*/) 
AND is_active = true