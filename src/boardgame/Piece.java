package boardgame;

public abstract class Piece {

	protected Position position;
	private Board board;
	
	public Piece(Board board) {
		this.board = board;
		position = null;
	}
	
	protected Board getBoard() {
		return board;
	}
	
	public abstract boolean[][] possibleMoves();
	
	public boolean possibleMove(Position position) {
		return possibleMoves()[position.getRow()][position.getColumn()];
	}
	
	public boolean isThereAnyPossibleMove() { //M�todo para percorrer a matriz de movimentos e verificar se a algum possivel movimento para a pe�a
		boolean [][] mat = possibleMoves(); //Matriz auxiliar para percorrer a matriz de posi��es
		for(int i = 0; i < mat.length; i++) {
			for(int j = 0; j < mat.length; j++) {
				if(mat[i][j]) { //Retorna verdadeiro caso haj� algum movimento possivel da pe�a na matriz
					return true;
				}
			}
		}
		return false;
	}
}
