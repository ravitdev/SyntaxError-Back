package com.syntaxerror.biblioteca.persistance.dao.impl;

import com.syntaxerror.biblioteca.persistance.dao.impl.base.DAOImplBase;
import com.syntaxerror.biblioteca.model.PersonasDTO;
import com.syntaxerror.biblioteca.model.PrestamosDTO;
import com.syntaxerror.biblioteca.model.enums.EstadoPrestamoEjemplar;
import com.syntaxerror.biblioteca.persistance.dao.impl.util.Columna;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.syntaxerror.biblioteca.persistance.dao.PrestamoDAO;

public class PrestamoDAOImpl extends DAOImplBase implements PrestamoDAO {

    private PrestamosDTO prestamo;

    public PrestamoDAOImpl() {
        super("BIB_PRESTAMOS");
        this.retornarLlavePrimaria = true;
        this.prestamo = null;
    }

    @Override
    protected void configurarListaDeColumnas() {
        this.listaColumnas.add(new Columna("ID_PRESTAMO", true, true));
        this.listaColumnas.add(new Columna("FECHA_SOLICITUD", false, false));
        this.listaColumnas.add(new Columna("FECHA_PRESTAMO", false, false));
        this.listaColumnas.add(new Columna("FECHA_DEVOLUCION", false, false));
        this.listaColumnas.add(new Columna("PERSONA_IDPERSONA", false, false));
    }

    @Override
    protected void incluirValorDeParametrosParaInsercion() throws SQLException {

        //si es autoincremental, se salta el (1,ID)
        this.statement.setDate(1, new Date(this.prestamo.getFechaSolicitud().getTime()));
        if (this.prestamo.getFechaPrestamo() != null) {
            this.statement.setDate(2, new Date(this.prestamo.getFechaPrestamo().getTime()));
        } else {
            this.statement.setDate(2, null);
        }

        if (this.prestamo.getFechaDevolucion() != null) {
            this.statement.setDate(3, new Date(this.prestamo.getFechaDevolucion().getTime()));
        } else {
            this.statement.setDate(3, null);
        }
        this.statement.setInt(4, this.prestamo.getPersona().getIdPersona());
    }

    @Override
    protected void incluirValorDeParametrosParaModificacion() throws SQLException {

        this.statement.setDate(1, new Date(this.prestamo.getFechaSolicitud().getTime()));
        if (this.prestamo.getFechaPrestamo() != null) {
            this.statement.setDate(2, new Date(this.prestamo.getFechaPrestamo().getTime()));
        } else {
            this.statement.setDate(2, null);
        }

        if (this.prestamo.getFechaDevolucion() != null) {
            this.statement.setDate(3, new Date(this.prestamo.getFechaDevolucion().getTime()));
        } else {
            this.statement.setDate(3, null);
        }
        this.statement.setInt(4, this.prestamo.getPersona().getIdPersona());
        this.statement.setInt(5, this.prestamo.getIdPrestamo());
        //En modificar el ID va al ultimo
    }

    @Override
    protected void incluirValorDeParametrosParaEliminacion() throws SQLException {
        this.statement.setInt(1, this.prestamo.getIdPrestamo());
        //Para eliminar solo va el id
    }

    @Override
    protected void incluirValorDeParametrosParaObtenerPorId() throws SQLException {
        this.statement.setInt(1, this.prestamo.getIdPrestamo());
        //Para obtener por Id igual solo el id
    }

    @Override
    protected void instanciarObjetoDelResultSet() throws SQLException {
        this.prestamo = new PrestamosDTO();
        this.prestamo.setIdPrestamo(this.resultSet.getInt("ID_PRESTAMO"));
        this.prestamo.setFechaSolicitud(this.resultSet.getDate("FECHA_SOLICITUD"));
        this.prestamo.setFechaPrestamo(this.resultSet.getDate("FECHA_PRESTAMO"));
        this.prestamo.setFechaDevolucion(this.resultSet.getDate("FECHA_DEVOLUCION"));

        // Crear objetos DTO básicos para las relaciones
        PersonasDTO persona = new PersonasDTO();
        persona.setIdPersona(this.resultSet.getInt("PERSONA_IDPERSONA"));
        this.prestamo.setPersona(persona);

    }

    @Override
    protected void limpiarObjetoDelResultSet() {
        this.prestamo = null;
    }

    @Override
    protected void agregarObjetoALaLista(List lista) throws SQLException {
        this.instanciarObjetoDelResultSet();
        lista.add(this.prestamo);
    }

    @Override
    public Integer insertar(PrestamosDTO prestamo) {
        this.prestamo = prestamo;
        return super.insertar();
    }

    @Override
    public PrestamosDTO obtenerPorId(Integer idPrestamo) {
        this.prestamo = new PrestamosDTO();
        this.prestamo.setIdPrestamo(idPrestamo);
        super.obtenerPorId();
        return this.prestamo;
    }

    @Override
    public ArrayList<PrestamosDTO> listarTodos() {
        return (ArrayList<PrestamosDTO>) super.listarTodos();
    }

    @Override
    public Integer modificar(PrestamosDTO prestamo) {
        this.prestamo = prestamo;
        return super.modificar();
    }

    @Override
    public Integer eliminar(PrestamosDTO prestamo) {
        this.prestamo = prestamo;
        return super.eliminar();
    }

    @Override
    public ArrayList<PrestamosDTO> listarPorIdPersona(int idPersona) {
        String sql = """
        SELECT %s
        FROM BIB_PRESTAMOS
        WHERE PERSONA_IDPERSONA = ?
    """.formatted(this.generarListaDeCampos());

        return (ArrayList<PrestamosDTO>) this.listarTodos(
                sql,
                obj -> {
                    try {
                        this.statement.setInt(1, (int) obj);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                idPersona
        );
    }

    @Override
    public List<PrestamosDTO> listarTodosPaginado(int limite, int offset) {
        String sql = String.format("""
        SELECT %s
        FROM BIB_PRESTAMOS
        ORDER BY ID_PRESTAMO
        LIMIT ? OFFSET ?
    """, this.generarListaDeCampos());

        return (List<PrestamosDTO>) this.listarTodos(
                sql,
                params -> {
                    int[] p = (int[]) params;
                    try {
                        this.statement.setInt(1, p[0]); // limite
                        this.statement.setInt(2, p[1]); // offset
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                new int[]{limite, offset}
        ); // Manejo de excepciones SQL si se requiere
    }

    @Override
    public List<PrestamosDTO> listarPorSedePaginado(int limite, int offset, int sedeId) {
        String sql = String.format("""
        SELECT DISTINCT %s
        FROM BIB_PRESTAMOS p
        JOIN BIB_PRESTAMOS_DE_EJEMPLARES pe ON p.ID_PRESTAMO = pe.PRESTAMO_IDPRESTAMO
        JOIN BIB_EJEMPLARES e ON pe.EJEMPLAR_IDEJEMPLAR = e.ID_EJEMPLAR
        WHERE e.Sede_IDSede = ?
        ORDER BY p.FECHA_SOLICITUD DESC
        LIMIT ? OFFSET ?
        """, this.generarListaDeCampos());

        return (List<PrestamosDTO>) this.listarTodos(
                sql,
                params -> {
                    int[] p = (int[]) params;
                    try {
                        this.statement.setInt(1, sedeId); // Sede ID
                        this.statement.setInt(2, p[0]); // limite
                        this.statement.setInt(3, p[1]); // offset
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                new int[]{limite, offset}
        );
    }

    @Override
    public List<PrestamosDTO> listarPrestamosPorEstadoPaginado(EstadoPrestamoEjemplar estado, int limite, int offset) {
        String sql = String.format("""
        SELECT DISTINCT %s
        FROM BIB_PRESTAMOS p
        JOIN BIB_PRESTAMOS_DE_EJEMPLARES pe ON p.ID_PRESTAMO = pe.PRESTAMO_IDPRESTAMO
        WHERE pe.ESTADO = ?
        ORDER BY p.FECHA_SOLICITUD DESC
        LIMIT ? OFFSET ?;
        """, this.generarListaDeCampos());

        return (List<PrestamosDTO>) this.listarTodos(
                sql,
                params -> {
                    try {
                        this.statement.setString(1, estado.name()); // Establecer el estado
                        this.statement.setInt(2, limite);    // Establecer el límite de resultados
                        this.statement.setInt(3, offset);    // Establecer el offset (página actual)
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                new int[]{limite, offset}
        );
    }

    @Override
    public String obtenerEstadoPrestamo(int idPrestamo) {
        String sql = """
            SELECT pe.ESTADO
            FROM BIB_PRESTAMOS_DE_EJEMPLARES pe
            JOIN BIB_PRESTAMOS p ON p.ID_PRESTAMO = pe.PRESTAMO_IDPRESTAMO
            WHERE p.ID_PRESTAMO = ?;
        """;

        // Ejecuta la consulta y devuelve el estado
        return (String) this.obtenerUnSoloValor(
                sql,
                params -> {
                    try {
                        this.statement.setInt(1, idPrestamo);  // Establecer el idPrestamo
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Override
    public List<PrestamosDTO> listarPrestamosPorEstadoYSedePaginado(EstadoPrestamoEjemplar estado, Integer sedeId, int limite, int offset) {
        StringBuilder sql = new StringBuilder(String.format("""
            SELECT DISTINCT %s
            FROM BIB_PRESTAMOS p
            JOIN BIB_PRESTAMOS_DE_EJEMPLARES pe ON p.ID_PRESTAMO = pe.PRESTAMO_IDPRESTAMO
            JOIN BIB_EJEMPLARES e ON pe.EJEMPLAR_IDEJEMPLAR = e.ID_EJEMPLAR
            WHERE pe.ESTADO = ?
        """, this.generarListaDeCampos()));

        // Si se especifica una sede válida (ej. ID > 0), se agrega al WHERE
        if (sedeId != -1) {
            sql.append(" AND e.SEDE_IDSEDE = ?");
        }

        sql.append("""
            ORDER BY p.FECHA_SOLICITUD DESC
            LIMIT ? OFFSET ?;
        """);

        return (List<PrestamosDTO>) this.listarTodos(
                sql.toString(),
                params -> {
                    try {
                        this.statement.setString(1, estado.name());

                        if (sedeId != -1) {
                            this.statement.setInt(2, sedeId);
                            this.statement.setInt(3, limite);
                            this.statement.setInt(4, offset);
                        } else {
                            this.statement.setInt(2, limite);
                            this.statement.setInt(3, offset);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                new int[]{limite, offset}
        );
    }

    @Override
    public int contarTotalPrestamos() {
        String sql = "SELECT COUNT(*) FROM BIB_PRESTAMOS;";
        return ((Long) this.obtenerUnSoloValor(sql, params -> {
        })).intValue();
    }

    @Override
    public int contarTotalPrestamosPorEstado(EstadoPrestamoEjemplar estado) {
        String sql = """
            SELECT COUNT(DISTINCT pe.PRESTAMO_IDPRESTAMO)
            FROM BIB_PRESTAMOS_DE_EJEMPLARES pe
            JOIN BIB_EJEMPLARES e ON pe.EJEMPLAR_IDEJEMPLAR = e.ID_EJEMPLAR        
            WHERE pe.ESTADO = ?;
        """;

        return ((Long) this.obtenerUnSoloValor(sql, params -> {
            try {
                this.statement.setString(1, estado.name());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        })).intValue();
    }

    @Override
    public int contarTotalPrestamosPorEstadoYSede(EstadoPrestamoEjemplar estado, int sedeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT pe.PRESTAMO_IDPRESTAMO)
            FROM BIB_PRESTAMOS_DE_EJEMPLARES pe
            JOIN BIB_EJEMPLARES e ON pe.EJEMPLAR_IDEJEMPLAR = e.ID_EJEMPLAR
            WHERE pe.ESTADO = ?
        """);

        if (sedeId != -1) {
            sql.append(" AND e.SEDE_IDSEDE = ?");
        }

        return ((Long) this.obtenerUnSoloValor(sql.toString(), params -> {
            try {
                this.statement.setString(1, estado.name());
                if (sedeId != -1) {
                    this.statement.setInt(2, sedeId);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        })).intValue();
    }

}
