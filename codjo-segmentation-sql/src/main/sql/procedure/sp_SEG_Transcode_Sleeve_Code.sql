if exists (select 1 from sysobjects where id = object_id('sp_SEG_Transcode_Sleeve_Code') and type = 'P')
begin
   drop proc sp_SEG_Transcode_Sleeve_Code
   print 'Procedure sp_SEG_Transcode_Sleeve_Code supprimee'
end
go

/************************************************************************************************************************/
/* Procédure permettant de transcoder un code poche (remplacement des caractères '.' et '-' par le caractère '_'	*/
/************************************************************************************************************************/

create proc sp_SEG_Transcode_Sleeve_Code (@SLEEVE_CODE varchar(50), @SLEEVE_CODE_TRANSCODED varchar(50) OUTPUT) as
begin

	select @SLEEVE_CODE_TRANSCODED = @SLEEVE_CODE

	/****************************************/
	/* Remplacement des '.' par des '_'	*/
	/****************************************/

	while (charindex('.',@SLEEVE_CODE_TRANSCODED) != 0)
		select @SLEEVE_CODE_TRANSCODED = stuff(@SLEEVE_CODE_TRANSCODED, charindex('.',@SLEEVE_CODE_TRANSCODED), 1, '_')

	/****************************************/
	/* Remplacement des '-' par des '_'	*/
	/****************************************/

	while (charindex('-',@SLEEVE_CODE_TRANSCODED) != 0)
		select @SLEEVE_CODE_TRANSCODED = stuff(@SLEEVE_CODE_TRANSCODED, charindex('-',@SLEEVE_CODE_TRANSCODED), 1, '_')

end
go

if exists (select 1 from sysobjects where id = object_id('sp_SEG_Transcode_Sleeve_Code') and type = 'P')
   print 'Procedure sp_SEG_Transcode_Sleeve_Code cree'
go

exec sp_procxmode 'sp_SEG_Transcode_Sleeve_Code','anymode'
print 'Procedure sp_SEG_Transcode_Sleeve_Code : transaction mode passée en anymode'
go