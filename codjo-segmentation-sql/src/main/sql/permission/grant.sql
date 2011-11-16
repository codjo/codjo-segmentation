grant select, insert, delete, update, references on PM_SEC_MODEL to Maintenance,Utilisateur
go
grant select, insert, delete, update, references on PM_CLASSIFICATION to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on PM_CLASSIFICATION_STRUCTURE to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on PM_EXPRESSION to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on PM_SEGMENTATION to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_RESULT_EVENT to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_RESULT_ACTION to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_RESULT_REQUETOR to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_INPUT_EVENT to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_INPUT_ACTION to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on SEG_INPUT_REQUETOR to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on LINKED_INPUT_REQUETOR to Utilisateur,Maintenance
go
grant select, insert, delete, update, references on AP_WORKFLOW_LOG to Utilisateur,Maintenance
go
grant execute on sp_SEG_Copy_AxisClassification to Maintenance,Utilisateur
go