if exists (select 1 from sysobjects where id = object_id('sp_SEG_Update_Includes') and type = 'P')
begin
   drop proc sp_SEG_Update_Includes
   print 'Procedure sp_SEG_Update_Includes supprimee'
end
go

/****************************************************************************************/
/* Procédure permettant de remplacer les includes par la formule de la poche référencée */
/****************************************************************************************/

create proc sp_SEG_Update_Includes (@EXPRESSION_ID int,
                                    @SLEEVE_NAME varchar(50),
                                    @CLASSIFICATION_ID int,
                                    @SLEEVE_ROW_ID varchar(20)) as
begin

    declare @FORMULA                   varchar(15000),
            @FORMULA_UPDATED           varchar(15000),
            @ALIAS_SLEEVE_NAME         varchar(50),
            @ALIAS_CLASSIFICATION_NAME varchar(50),
            @ALIAS_FORMULA             varchar(15000),
            @ALIAS_CLASSIFICATION_ID   int,
            @ALIAS_START_INDEX         int,
            @INDEX                     int,
            @ALIAS_SLEEVE_ROW_ID       varchar(20),
            @errmsg                    varchar(500)

    select @FORMULA = convert(varchar(15000), EXPRESSION)
    from PM_EXPRESSION
    where EXPRESSION_ID = @EXPRESSION_ID

    while (charindex("INC_$$", @FORMULA) != 0)
    begin

        -- Détermination de la 1ère position des inclusions dans la formule
    	select @ALIAS_START_INDEX = charindex("INC_$$", @FORMULA)

        -- Début de la formule sans le INC_$$
        select @INDEX=char_length(@FORMULA)-@ALIAS_START_INDEX-5
        select @FORMULA_UPDATED = right(@FORMULA, @INDEX)


	    -- Identification de l'axe et du SLEEVE_ROW_ID référencés
		select @ALIAS_CLASSIFICATION_ID = convert(int, substring(@FORMULA_UPDATED, 1, charindex("$", @FORMULA_UPDATED)-1)),
			   @ALIAS_SLEEVE_ROW_ID = substring(@FORMULA_UPDATED, charindex("$", @FORMULA_UPDATED) + 1, 13)

        -- Sélection des données référencées
        select @ALIAS_FORMULA = convert(varchar(15000),FORMULA),
        		@ALIAS_SLEEVE_NAME = SLEEVE_NAME,
        		@ALIAS_CLASSIFICATION_NAME = CLASSIFICATION_NAME
        from PM_CLASSIFICATION_STRUCTURE cs
        	inner join PM_CLASSIFICATION c
        		on (cs.CLASSIFICATION_ID = c.CLASSIFICATION_ID)
        where cs.CLASSIFICATION_ID = @ALIAS_CLASSIFICATION_ID
        and SLEEVE_ROW_ID = @ALIAS_SLEEVE_ROW_ID

        -- Test s'il existe des références cycliques
        if(charindex("INC_$$" + convert(varchar, @CLASSIFICATION_ID) + "$" + @SLEEVE_ROW_ID, @ALIAS_FORMULA) != 0 )
        begin
            select @errmsg = "Vous ne pouvez pas créer de référence cyclique entre la poche '"
                + @SLEEVE_NAME
                + "' de cet axe et la poche '"
                + @ALIAS_SLEEVE_NAME
                + "' de l'axe '"
                + @ALIAS_CLASSIFICATION_NAME
                + "'."

            rollback transaction
            raiserror 999999 @errmsg
            return
        end

        -- Suppression de la chaine "INC_$$" pour la 1ère des inclusions dans la formule
        -- nécessaire pour identifier l'axe le SLEEVE_ROW_ID référencés
        select @FORMULA = stuff(@FORMULA, @ALIAS_START_INDEX, char_length("INC_$$"), null)
        
        -- Remplacement de l'alias par la vraie formule
		select @FORMULA_UPDATED = stuff(@FORMULA,
		                                @ALIAS_START_INDEX,
                                        char_length(convert(varchar, @ALIAS_CLASSIFICATION_ID) + "$" + @ALIAS_SLEEVE_ROW_ID),
                                        @ALIAS_FORMULA)

        select @FORMULA = @FORMULA_UPDATED
    end

    update PM_EXPRESSION
        set EXPRESSION = @FORMULA
    from PM_EXPRESSION
    where EXPRESSION_ID = @EXPRESSION_ID
        and @FORMULA != NULL

end
go

if exists (select 1 from sysobjects where id = object_id('sp_SEG_Update_Includes') and type = 'P')
   print 'Procedure sp_SEG_Update_Includes cree'
go

exec sp_procxmode 'sp_SEG_Update_Includes','anymode'
print 'Procedure sp_SEG_Update_Includes : transaction mode passée en anymode'
go