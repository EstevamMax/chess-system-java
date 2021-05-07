package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		initialSetup();
	}
	
	public int getTurn() {
		return turn;
	}
	
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for(int i = 0; i < board.getRows(); i++) {
			for(int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition) { //retorna uma matriz de booleans que mostram os possiveis movimentos da pe�a indicada
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) { //M�todo que realiza todo o movimento no xadrez
		Position source = sourcePosition.toPosition(); //Convertendo a posi��o informada para posi��o de matriz
		Position target = targetPosition.toPosition();
		validateSourcePosition(source); //Verificando se a posi��o informada possui pe�a ou n�o
		validateTargetPosition(source, target); //Se a posi��o de destino � realmente valida para alocar a pe�a
		Piece capturedPiece = makeMove(source, target); //Capturando a pe�a na posi��o informada
		nextTurn(); //M�todo que incrementa o turno atual na partida e muda o jogador
		return (ChessPiece)capturedPiece; //Downcast de Piece para ChessPiece
	}
	
	private Piece makeMove(Position source, Position target) {
		Piece p = board.removePiece(source); //remove a pe�a da posi��o inicial
		Piece capturedPiece = board.removePiece(target); //move a pe�a para a posi��o desejada capturando a pe�a que estiver l�
		board.placePiece(p, target); //colocando a pe�a desejada na posi��o informada anteriormente
		return capturedPiece;
	}
	
	private void validateSourcePosition(Position position) {
		if(!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position");
		}
		if(currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours");
		}
		if(!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece");
		}
	}
	
	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position");
		}
	}
	
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE; //Se o jogador atual for igual ao branco ent�o ele mudar� para o preto, caso contr�rio mudara para o branco
	}
	
	private void placeNewPiece(char column, int row, ChessPiece piece) { //M�todo para adicionar pe�as ao jogo
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
	}
	
	private void initialSetup() { //Instancia��o das pe�as no in�cio da partida.
		placeNewPiece('c', 1, new Rook(board, Color.WHITE));
        placeNewPiece('c', 2, new Rook(board, Color.WHITE));
        placeNewPiece('d', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new King(board, Color.WHITE));

        placeNewPiece('c', 7, new Rook(board, Color.BLACK));
        placeNewPiece('c', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 8, new King(board, Color.BLACK));
	}
}
