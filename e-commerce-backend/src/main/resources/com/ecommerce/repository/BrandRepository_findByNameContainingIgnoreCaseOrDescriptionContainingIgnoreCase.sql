SELECT * FROM brands 
WHERE (LOWER(name) LIKE LOWER('%' || /*search*/ || '%') OR 
       LOWER(description) LIKE LOWER('%' || /*search*/ || '%'))