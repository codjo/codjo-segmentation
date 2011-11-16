if exists (select 1 from sysobjects where id = object_id('sp_SEG_Copy_AxisClassification') and type = 'P')
begin
   drop proc sp_SEG_Copy_AxisClassification
   print 'Procedure sp_SEG_Copy_AxisClassification supprimee'
end
go

/************************************************************************************************************************/
/* Procédure permettant de transcoder un code poche (remplacement des caractères '.' et '-' par le caractère '_'	*/
/************************************************************************************************************************/

create proc sp_SEG_Copy_AxisClassification (@CLASSIFICATION_ID int) as
begin
    declare @SLEEVE_ID          int --variable qui récupérera les ID
            ,@NEXT_SLEEVE_ID    int
            ,@NEXT_CLASS_ID     int

    -- recopie des entrées de la table PM_CLASSIFICATION dont l'ID est égal à @CLASSIFICATION_ID
    --  On change le CLASSIFICATION_ID et le CLASSIFICATION_NAME
    select  @NEXT_CLASS_ID = (max(CLASSIFICATION_ID)+1) from PM_CLASSIFICATION
    insert into PM_CLASSIFICATION
        ( CLASSIFICATION_ID,
          CLASSIFICATION_NAME,
          CLASSIFICATION_TYPE
        )
        select
            @NEXT_CLASS_ID,
            'Copie (' + convert(varchar, @NEXT_CLASS_ID) + ') de ' + CLASSIFICATION_NAME,
            CLASSIFICATION_TYPE
        from PM_CLASSIFICATION where CLASSIFICATION_ID = @CLASSIFICATION_ID

    -- recopie ligne à ligne des entrées de la table PM_CLASSIFICATION_STRUCTURE dont le CLASSIFICATION_ID
    -- est égal à @CLASSIFICATION_ID
    declare sleeve_cursor CURSOR -- déclaration du curseur On sélectionne toutes les lignes a recopier
    for select SLEEVE_ID from PM_CLASSIFICATION_STRUCTURE
        where CLASSIFICATION_ID = @CLASSIFICATION_ID order by SLEEVE_ID

    open sleeve_cursor
    fetch sleeve_cursor into @SLEEVE_ID

    while @@sqlstatus=0
 	begin
        select  @NEXT_SLEEVE_ID = (max(SLEEVE_ID)+1) from PM_CLASSIFICATION_STRUCTURE

        insert into PM_CLASSIFICATION_STRUCTURE
            ( SLEEVE_ID,
              SLEEVE_ROW_ID,
              CLASSIFICATION_ID,
              SLEEVE_CODE,
              SLEEVE_NAME,
              SLEEVE_DUSTBIN,
              TERMINAL_ELEMENT,
              FORMULA
            )
	        select
	            @NEXT_SLEEVE_ID ,
	            SLEEVE_ROW_ID ,
	            @NEXT_CLASS_ID,
	            SLEEVE_CODE,
	            SLEEVE_NAME,
	            SLEEVE_DUSTBIN,
	            TERMINAL_ELEMENT,
	            FORMULA
	        from PM_CLASSIFICATION_STRUCTURE where SLEEVE_ID = @SLEEVE_ID

        -- On sélectionne la ligne a recopier suivante
        fetch sleeve_cursor into @SLEEVE_ID
    end
    -- Désallocation du curseur
    close sleeve_cursor
    deallocate cursor sleeve_cursor
end
go

if exists (select 1 from sysobjects where id = object_id('sp_SEG_Copy_AxisClassification') and type = 'P')
   print 'Procedure sp_SEG_Copy_AxisClassification cree'
go

exec sp_procxmode 'sp_SEG_Copy_AxisClassification','anymode'
print 'Procedure sp_SEG_Copy_AxisClassification : transaction mode passée en anymode'
go