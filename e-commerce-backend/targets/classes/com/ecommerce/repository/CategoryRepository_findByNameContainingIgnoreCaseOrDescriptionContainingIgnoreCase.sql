SELECT * FROM categories 
WHERE (LOWER(name) LIKE LOWER('%' || /*search*/ || '%') OR 
       LOWER(description) LIKE LOWER('%' || /*search*/ || '%'))