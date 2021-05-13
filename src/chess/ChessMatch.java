package chess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();
	
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
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	
	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece getPromoted() {
		return promoted;
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
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition) { //retorna uma matriz de booleans que mostram os possiveis movimentos da peça indicada
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) { //Método que realiza todo o movimento no xadrez
		Position source = sourcePosition.toPosition(); //Convertendo a posição informada para posição de matriz
		Position target = targetPosition.toPosition();
		validateSourcePosition(source); //Verificando se a posição informada possui peça ou não
		validateTargetPosition(source, target); //Se a posição de destino é realmente valida para alocar a peça
		Piece capturedPiece = makeMove(source, target); //Capturando a peça na posição informada
		
		if(testCheck(currentPlayer)) { //Testa se o jogador esta se colocando em check
			undoMove(source, target, capturedPiece);
			throw new ChessException("You can't put yourself in check");
		}
		
		check = (testCheck(opponent(currentPlayer))) ? true : false; //Se o oponente estiver em check retorna true, caso contrário false
		
		ChessPiece movedPiece = (ChessPiece)board.piece(target);
		
		//#Specialmove promotion
		promoted = null;
		if(movedPiece instanceof Pawn) {
			if(movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK && target.getRow() == 7) {
				promoted = (ChessPiece)board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		if(testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		}
		else {
			nextTurn(); //Método que incrementa o turno atual na partida e muda o jogador
		}
		
		//#Specialmove en passant
		if(movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) { //Teste para saber se a peça movida pod sofrer o en passant
			enPassantVulnerable = movedPiece;
		} else {
			enPassantVulnerable = null;
		}
	
		return (ChessPiece)capturedPiece; //Downcast de Piece para ChessPiece
	}
	
	public ChessPiece replacePromotedPiece(String type) {
		if (promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted");
		}
		if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) { //String é um tipo classe, e não primitivo, por isso o uso de "equals"
			throw new InvalidParameterException("Invalid type for promotion");
		}
		
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		
		ChessPiece newPiece = newPiece(type, promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);
		
		return newPiece;
	}
	
	private ChessPiece newPiece(String type, Color color) {
		if(type.equals("B")) return new Bishop(board, color);
		if(type.equals("N")) return new Knight(board, color);
		if(type.equals("Q")) return new Queen(board, color);
		return new Rook(board, color);
	}
	
	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.removePiece(source); //remove a peça da posição inicial
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target); //move a peça para a posição desejada capturando a peça que estiver lá
		board.placePiece(p, target); //colocando a peça desejada na posição informada anteriormente
		
		if(capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		//#Specialmove castling kingside rook
		if(p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3); //Pegando a torre na posição inicial do lado do rei
			Position targetT = new Position(source.getRow(), source.getColumn() + 1); //definindo a nova posição da torre após o roque
			ChessPiece rook = (ChessPiece)board.removePiece(sourceT); //Criando variavel auxiliar para pegar e mover a torre
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		
		//#Specialmove castling queenside rook
				if(p instanceof King && target.getColumn() == source.getColumn() - 2) {
					Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
					Position targetT = new Position(source.getRow(), source.getColumn() - 1);
					ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
					board.placePiece(rook, targetT);
					rook.increaseMoveCount();
				}
			
		//#Specialmove en passant
		if(p instanceof Pawn) {
			if(source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if(p.getColor() == Color.WHITE) {
					pawnPosition = new Position(target.getRow() + 1, target.getColumn());
				} else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				piecesOnTheBoard.remove(capturedPiece);
				capturedPieces.add(capturedPiece);
			}
		}
		
		return capturedPiece;
	}
	
	private void undoMove(Position source, Position target, Piece capturedPiece) { //Método para desfazer o movimento feito anteriormente em "makeMove"
		ChessPiece p = (ChessPiece)board.removePiece(target); //Remove a peça da posição ocupada anteriormente
		p.decreaseMoveCount();
		board.placePiece(p, source); //volta ela para a posição inicial
		
		if(capturedPiece != null) { //verifica se houve alguma captura de peça
			board.placePiece(capturedPiece, target); //coloca a peça capturada no destino
			capturedPieces.remove(capturedPiece); //remove a peça capturada da lista de capturas
			piecesOnTheBoard.add(capturedPiece); //coloca de volta na lista de peças em jogo
		}
		
		//#Specialmove castling kingside rook
		if(p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
				
		//#Specialmove castling queenside rook
		if(p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		
		//#Specialmove en passant
		if(p instanceof Pawn) {
			if(source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece)board.removePiece(target);
				Position pawnPosition;
				if(p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				} else {
					pawnPosition = new Position(4, target.getColumn());
				}
				board.placePiece(pawn, pawnPosition);
				
				capturedPiece = board.removePiece(pawnPosition);
			}
		}
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
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE; //Se o jogador atual for igual ao branco então ele mudará para o preto, caso contrário mudara para o branco
	}
	
	private Color opponent(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList()); //Faz uma lista com todas as peças da cor passada
		for(Piece p : list) { //Percorre a lista e retorna o rei caso ele esteja em jogo
			if(p instanceof King) {
				return (ChessPiece)p;
			}
		}
		throw new IllegalStateException("There is no " + color + " King on the board");
	}
	
	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition(); //Pega a posição do rei em formato de matriz
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		for(Piece p : opponentPieces) { //Percorre a lista criada anteriormente
			boolean[][] mat = p.possibleMoves(); //Cria uma matriz com os possiveis movimentos das peças opostas
			if(mat[kingPosition.getRow()][kingPosition.getColumn()]) { //Caso haja alguma peça que tem o rei como alvo e possível movimento o método retorna true
				return true;
			}
		}
		return false;
	}
	
	private boolean testCheckMate(Color color) {
		if(!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for(Piece p : list) {
			boolean[][] mat = p.possibleMoves(); //Matriz auxiliar que pega todos os movimentos possiveis de todas as peças da cor indicada
			for (int i = 0; i < board.getRows(); i++) {
				for(int j = 0; j < board.getColumns(); j++) {
					if(mat[i][j]) { //O if será ativado sempre que alguma peça da matriz ter movimentos possiveis (retornar true)
						Position source = ((ChessPiece)p).getChessPosition().toPosition(); //Variável auxiliar que salva a posição da peça 'p'
						Position target = new Position(i, j); //Váriavel auxiliar que salva o movimento possível identificado na matriz
						Piece capturedPiece = makeMove(source, target); //Auxiliar que faz o movimento identificado anteriormente
						boolean testCheck = testCheck(color);
						undoMove(source, target, capturedPiece); //Disfaz todo o movimento anterior
						if(!testCheck) { //Caso continue em check retorna true, pelo contrário retorna false e nega o check mate
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private void placeNewPiece(char column, int row, ChessPiece piece) { //Método para adicionar peças ao jogo
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}
	
	private void initialSetup() { //Instanciação das peças no início da partida.
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE,this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE,this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));
		
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}
}
