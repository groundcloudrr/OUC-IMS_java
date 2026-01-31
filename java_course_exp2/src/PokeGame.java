package ouc.cs.java.pokegame;

import java.util.*;

public class PokeGame implements Action {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("How many decks of cards are needed? ");
        int decks = sc.nextInt();

        Poke cardSet = new Poke(decks);
        List<Card> cards = cardSet.getCards();

        System.out.print("How many players in the poker game? ");
        int players = sc.nextInt();
        sc.close();

        Action game = new PokeGame();
        game.shuffle(cards);
        List<List<Card>> hands = game.distribute(cards, players);

        System.out.println("\nDisplay all cards:");
        game.display(cards);

        System.out.println("\nDisplay cards assigned to each player:");
        for (int i = 0; i < hands.size(); i++) {
            List<Card> hand = hands.get(i);
            Collections.sort(hand);
            System.out.println("Player " + (i + 1) + ":");
            game.display(hand);
        }

        System.out.println("\nRandomly draw 2 cards from all cards and compare their values:");
        Random rand = new Random();
        int idx1 = rand.nextInt(cards.size());
        int idx2;
        do {
            idx2 = rand.nextInt(cards.size());
        } while (idx1 == idx2);

        Card c1 = cards.get(idx1);
        Card c2 = cards.get(idx2);
        System.out.println("Drew: " + c1 + " and " + c2);

        int cmp = c1.compareTo(c2);
        String result;
        if (cmp < 0) {
            result = "The first card is smaller";
        } else if (cmp > 0) {
            result = "The first card is larger";
        } else {
            result = "Both cards are equal";
        }
        System.out.println(result);
    }

    @Override
    public void shuffle(List<Card> cards) {
        Collections.shuffle(cards);
    }

    @Override
    public List<List<Card>> distribute(List<Card> cards, int players) {
        List<List<Card>> hands = new ArrayList<>(players);
        for (int i = 0; i < players; i++) {
            hands.add(new ArrayList<>());
        }

        int idx = 0;
        for (Card card : cards) {
            hands.get(idx++ % players).add(card);
        }
        return hands;
    }

    @Override
    public void display(List<Card> cards) {
        for (Card c : cards) {
            System.out.println(c);
        }
    }
}

enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

enum Face {
    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6),
    SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13);

    private final int val;
    Face(int v) { val = v; }
    public int getValue() { return val; }
}

class Card implements Comparable<Card> {
    private final Suit suit;
    private final Face face;

    public Card(Suit s, Face f) {
        suit = s;
        face = f;
    }

    public Suit getSuit() { return suit; }
    public Face getFace() { return face; }

    @Override
    public int compareTo(Card other) {
        return Integer.compare(face.getValue(), other.face.getValue());
    }

    @Override
    public String toString() {
        return face + " of " + suit;
    }
}

class Poke {
    private final List<Card> cards = new ArrayList<>();

    public Poke() {
        this(1);
    }

    public Poke(int decks) {
        for (int d = 0; d < decks; d++) {
            for (Suit s : Suit.values()) {
                for (Face f : Face.values()) {
                    cards.add(new Card(s, f));
                }
            }
        }
    }

    public List<Card> getCards() {
        return cards;
    }
}

interface Action {
    void shuffle(List<Card> cards);
    List<List<Card>> distribute(List<Card> cards, int players);
    void display(List<Card> cards);
}