package com.example.alcoopoly.data

import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card

object CardData {

    // Liste initiale des cartes CHANCE
    val initialChanceCards = listOf(
        Card(1, "Invente une règle qui s'applique jusqu'à la fin de la partie.", CardType.CHANCE),
        Card(2, "Le joueur à ta droite boit 3 gorgées.", CardType.CHANCE),
        Card(3, "Tu es élu 'Barman'. Tu dois servir tout le monde jusqu'au prochain tour.", CardType.CHANCE),
        Card(4, "Distribution générale : Distribue 5 gorgées comme tu veux.", CardType.CHANCE),
        Card(5, "Immunité : Tu peux refuser la prochaine gorgée qu'on te donne.", CardType.CHANCE),
        Card(6, "Tout le monde boit 2 gorgées sauf toi.", CardType.CHANCE),
        Card(7, "Choisis un 'Partenaire de boisson'. Il boit à chaque fois que tu bois.", CardType.CHANCE),
        Card(8, "Duel de regard avec le joueur en face. Le perdant boit 4 gorgées.", CardType.CHANCE)
    )

    // Liste initiale des cartes MINI-JEU
    val initialMiniGameCards = listOf(
        Card(101, "Je n'ai jamais... (3 tours de table)", CardType.MINI_JEU),
        Card(102, "Dans ma valise... (Le perdant boit 3)", CardType.MINI_JEU),
        Card(103, "Thème : Marques de voitures. Le premier qui sèche boit 3.", CardType.MINI_JEU),
        Card(104, "Shi-Fu-Mi géant. Les perdants boivent 2.", CardType.MINI_JEU),
        Card(105, "Le sol est de la lave ! Le dernier monté boit 4.", CardType.MINI_JEU),
        Card(106, "Ni OUI, Ni NON jusqu'à ton prochain tour. 1 gorgée par erreur.", CardType.MINI_JEU)
    )
}