CREATE OR REPLACE FUNCTION cascade_soft_delete_league()
    RETURNS TRIGGER AS $$
BEGIN
    -- Comprueba si el deleted_at acaba de ser actualizado a un valor no nulo
    IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
        -- Aplica el soft delete a los equipos asociados
        UPDATE team
        SET deleted_at = NEW.deleted_at
        WHERE league_id = NEW.id AND deleted_at IS NULL;

        -- Puedes añadir más tablas aquí abajo si es necesario (participaciones, etc.)
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_soft_delete_league
    AFTER UPDATE OF deleted_at ON league
    FOR EACH ROW
EXECUTE FUNCTION cascade_soft_delete_league();