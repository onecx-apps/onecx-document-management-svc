insert into dm_document_type (guid, creationdate, creationuser,optlock,name) values ((SELECT uuid_in(md5(random()::text || random()::text)::cstring)),now() ,'Monalisha',0,'Personal Document');
insert into dm_document_type (guid, creationdate, creationuser,optlock,name) values ((SELECT uuid_in(md5(random()::text || random()::text)::cstring)),now() ,'Monalisha',0,'Financial Document');
insert into dm_document_type (guid, creationdate, creationuser,optlock,name) values ((SELECT uuid_in(md5(random()::text || random()::text)::cstring)),now() ,'Monalisha',0,'Company Document');