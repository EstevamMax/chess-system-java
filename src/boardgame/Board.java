package boardgame;

public class Board {

	private int rows;
	private int columns;
	private Piece[][] pieces;
	
	public Board(int rows, int columns) {
		if(rows < 1 || columns < 1) {
			throw new BoardException("Error creating board: there must be at least 1 row and 1 column");
		}
		this.rows = rows;
		this.columns = columns;
		pieces = new Piece[rows][columns];
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public Piece piece(int row, int column) {
		if(!positionExists(row, column)) { //Caso a posição NÃO (!) exista fazer:
			throw new BoardException("Position not on the board");
		}
		return pieces[row][column];
	}
	
	public Piece piece(Position position) {
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board");
		}
		return pieces[position.getRow()][position.getColumn()];
	}
	
	public void placePiece(Piece piece, Position position) {
		if(thereIsAPiece(position)) { //Caso já aja uma peça naquela posição fazer:
			throw new BoardException("There is already a piece on position " + position);
		}
		pieces[position.getRow()][position.getColumn()] = piece;
		piece.position = position;
	}
	
	public Piece removePiece(Position position) { //Método para remover a peça da posição atual
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board");
		}
		if(piece(position) == null) { //Indica se há uma peça na posição informada antes de ser retirada
			return null;
		}
		Piece aux = piece(position); //Caso haja uma peça na posição informada "pega" ela com essa variável auxiliar
		aux.position = null;
		pieces[position.getRow()][position.getColumn()] = null; //Informa que não há mais peça na dita posição da matriz que continha a peça retirada
		return aux;
	}
	
	public boolean positionExists(int row, int column) { //Método para testar se a informada existe no tabuleiro
		return row >= 0 && row < rows && column >= 0 && column < columns;
	}
	
	public boolean positionExists(Position position) {
		return positionExists(position.getRow(), position.getColumn());
	}
	
	public boolean thereIsAPiece(Position position) { //Método para testar se já existe uma peça na posição informada
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board");
		}
		return piece(position) != null;
	}
}
