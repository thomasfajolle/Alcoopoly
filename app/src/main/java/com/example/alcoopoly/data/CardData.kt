package com.example.alcoopoly.data

import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card

object CardData {

    // --- MINI-JEUX ---
    val initialMiniGameCards = listOf(
        // Bataille de regard (x1)
        Card(1, "👀 BATAILLE DE REGARD\nLe perdant boit 5 gorgées.", CardType.MINI_JEU),

        // Dans ma valise (x1)
        Card(2, "🧳 DANS MA VALISE\nLe premier qui oublie un objet boit 3.", CardType.MINI_JEU),

        // Qui pourrait (x1)
        Card(3, "👉 QUI POURRAIT ?\nLance un 'Qui pourrait...'. À 3, tout le monde pointe quelqu'un.\nLa personne visée boit le nombre de votes reçus.", CardType.MINI_JEU),

        // 1024 (x1)
        Card(4, "📱 1024\nTout le monde tape un nombre entre 1 et 1024 sur son tel.\nSi doublon : ils boivent 2.\nSi quelqu'un a mis 1 ou 1024 : il distribue 2.\nEnsuite entre 1-512, 1-256...", CardType.MINI_JEU),

        // Bite Chatte Couilles (x1)
        Card(5, "🐱 BITE - CHATTE - COUILLES\nFaites 5 tours. Si vous connaissez pas les règles, cherchez sur Internet ou inventez !", CardType.MINI_JEU),

        // 3 anecdotes (x1)
        Card(6, "🤥 3 ANECDOTES\nRaconte 2 vraies, 1 fausse. Les autres devinent la fausse.\nCeux qui se trompent boivent 5.\nSi personne ne se trompe : CUL SEC pour toi !", CardType.MINI_JEU),

        // Tu préfères (x2)
        Card(7, "⚖️ TU PRÉFÈRES ?\nPose un dilemme. La minorité boit le nombre de gorgées de la majorité.\nEx: 5 vs 2 -> Les 2 boivent 5.", CardType.MINI_JEU),
        Card(8, "⚖️ TU PRÉFÈRES ? (Le Retour)\nPose un dilemme. La minorité boit le nombre de gorgées de la majorité.", CardType.MINI_JEU),

        // PMU (x1)
        Card(9, "🐎 PMU\nSortez 4 valets. Pariez des gorgées sur une couleur (buvez pour valider).\n1er : Distribue le double.\n2e : Distribue la mise.\n3e : Boit la mise.\n4e : Boit le double.", CardType.MINI_JEU),

        // Jeu de la pièce (x3)
        Card(10, "🪙 JEU DE LA PIÈCE\nFaites rebondir une pièce... (Si vous savez pas jouer, passez à un autre jeu !)", CardType.MINI_JEU),
        Card(11, "🪙 JEU DE LA PIÈCE\nC'est reparti pour un tour !", CardType.MINI_JEU),
        Card(12, "🪙 JEU DE LA PIÈCE\nEncore une fois !", CardType.MINI_JEU),

        // Juste Cuite (x1)
        Card(13, "🧪 JUSTE CUITE\nLe maître de maison te prépare un shot de potion pure.\nDevine le degré d'alcool.\nTu bois la différence entre ton estimation et le réel (ex: dit 40°, réel 70° -> 30 gorgées !).", CardType.MINI_JEU),

        // Rime (x1)
        Card(14, "📝 RIME\nTu commences avec un mot. Chacun doit trouver une rime.\nLe premier qui sèche ou répète boit 3.", CardType.MINI_JEU),

        // 99 (x1)
        Card(15, "🔢 LE 99\nComptez jusqu'à 99 avec les cartes (Valet +/-10, Dame sens, Roi 70, As 1/11).\nDizaine pile : tu donnes le chiffre.\nDizaine dépassée : tu bois le chiffre.\n99 atteint ou dépassé : CUL SEC.", CardType.MINI_JEU),

        // Invente (x1)
        Card(16, "💡 CRÉATIF\nInvente un mini-jeu ou refais-en un que tu as kiffé.", CardType.MINI_JEU),

        // Je n'ai jamais (x3)
        Card(17, "😇 JE N'AI JAMAIS\nDis un 'Je n'ai jamais...'. Ceux qui l'ont fait boivent 2.", CardType.MINI_JEU),
        Card(18, "😇 JE N'AI JAMAIS\nEncore un tour ! Ceux qui l'ont fait boivent 2.", CardType.MINI_JEU),
        Card(19, "😇 J'AI DÉJÀ\nDis un 'J'ai déjà...'. Ceux qui ne l'ont pas fait boivent 2.", CardType.MINI_JEU),

        // Vod'keau (x2)
        Card(20, "💧 VOD'KEAU\nToi + 2 joueurs. 1 shot de vodka, 2 d'eau. Buvez (Poker Face).\nLe public parie qui a la vodka. Les perdants boivent un shot.", CardType.MINI_JEU),
        Card(21, "💧 VOD'KEAU (Revanche)\nOn remet ça ! 1 vodka, 2 eaux. Devinez qui a l'alcool.", CardType.MINI_JEU)
    )

    // --- CARTES CHANCE ---
    val initialChanceCards = listOf(
        // Maître du Limousin
        Card(101, "👑 MAÎTRE DU LIMOUSIN\nBois 5 gorgées maintenant. En échange, tu peux lancer un Limousin à qui tu veux, quand tu veux (1 fois).", CardType.CHANCE),

        // Vol de propriété
        Card(102, "🏴‍☠️ VOL DE PROPRIÉTÉ\nChoisis une victime. Lancez les dés.\nSi tu fais strictement plus qu'elle, vole-lui la propriété de ton choix.", CardType.CHANCE),

        // Chi Fou Bois
        Card(103, "👊 CHI-FOU-BOIS\nDésigne 2 personnes. Elles s'affrontent au Chi-Fou-Mi.\nLe perdant boit 2 gorgées par manche (les égalités s'accumulent !).", CardType.CHANCE),

        // Couple improbable
        Card(104, "💘 COUPLE IMPROBABLE\nQui aurait le plus de chance de se pécho ici ? Choisis 2 personnes.\nElles sont liées pour 2 tours : si l'une boit, l'autre boit.", CardType.CHANCE),

        // Black-out partiel (Soirée BDE - Case 36)
        Card(105, "😵 BLACK-OUT PARTIEL\nTu reprends conscience en fin de soirée BDE.\nAvance directement à la case 'Soirée BDE' (Case 36).", CardType.CHANCE),

        // Black-out total (Retour départ)
        Card(106, "💀 BLACK-OUT TOTAL\nTu ne te souviens de RIEN.\nRetourne à la Cave Départ. Tu ne donnes rien (car tu n'as rien).", CardType.CHANCE),

        // After Farigoule (Bassine)
        Card(107, "🌿 AFTER FARIGOULE\nTu finis en after chelou.\nBois la Bassine de ton plein gré.\n(Si elle est vide, bois 3 gorgées de ton verre).", CardType.CHANCE),

        // Vol de vêtement
        Card(108, "👕 VOL À L'ÉTALAGE\nVole un vêtement à la personne de ton choix et mets-le sur toi.", CardType.CHANCE),

        // Pour combien
        Card(109, "🤔 POUR COMBIEN ?\nLance un 'Pour combien ?' à la personne en face de toi.\n(Attention au contre-uno /2 !)", CardType.CHANCE),

        // Confessions
        Card(110, "🤫 CONFESSIONS NOCTURNES\nTu es bourré. Raconte une anecdote honteuse sur toi ou bois 5 gorgées.", CardType.CHANCE),

        // Chope Gauche / Droite
        Card(111, "👈 GAUCHE\nTu chopes le joueur à ta gauche.\nPendant 1 tour : Si tu bois, il boit. S'il boit, tu bois.", CardType.CHANCE),
        Card(112, "👉 DROITE\nTu chopes le joueur à ta droite.\nPendant 1 tour : Si tu bois, il boit. S'il boit, tu bois.", CardType.CHANCE),

        // Partenaire
        Card(113, "🤝 PARTENAIRE\nChoisis ton partenaire de boisson.\nPendant 1 tour, s'il boit, tu bois (mais pas l'inverse).", CardType.CHANCE),

        // Anniversaire
        Card(114, "🎂 JOYEUX ANNIVERSAIRE !\nC'est ton jour (ou pas). CUL SEC !", CardType.CHANCE),

        // Distributions
        Card(115, "🎁 CADEAU\nDonne 4 gorgées à qui tu veux.", CardType.CHANCE),
        Card(116, "🏙️ MAGNAT DE L'IMMOBILIER\nDonne 4 gorgées à celui/celle qui possède le plus de propriétés.", CardType.CHANCE),
        Card(117, "🎁 GROS CADEAU\nDonne 5 gorgées à qui tu veux.", CardType.CHANCE),
        Card(118, "🏙️ ROI DU PÉTROLE\nDonne 5 gorgées à celui/celle qui possède le plus de propriétés.", CardType.CHANCE),
        Card(119, "⛺ SDF\nDonne 5 gorgées à celui/celle qui possède le MOINS de propriétés.", CardType.CHANCE),

        // Action Vérité
        Card(120, "😈 ACTION OU VÉRITÉ\nLes autres choisissent pour toi.\nRefus = CUL SEC.", CardType.CHANCE),

        // Rentrer chez lui
        Card(121, "🥴 SAM\nQui a le moins de chance de rentrer chez lui ce soir ?\nIl/Elle donne 3 gorgées à qui il veut.", CardType.CHANCE),

        // Vol d'identité
        Card(122, "🎭 VOL D'IDENTITÉ\nTu peux prendre la place de quelqu'un.\nTu récupères TOUT : ses propriétés, son verre, sa dignité...", CardType.CHANCE),

        // Générales
        Card(123, "🍻 SANTÉ !\nTout le monde boit 1 gorgée (même toi).", CardType.CHANCE),
        Card(124, "🥂 À LA TIENNE !\nTout le monde boit 2 gorgées en ton honneur.", CardType.CHANCE),

        // Dealer
        Card(125, "🏃 DEALER D'EN BAS\nTu te fais aborder. Pour fuir, lance les dés jusqu'à faire un double.\nBois 2 gorgées par essai raté.", CardType.CHANCE),

        // Bar'ban
        Card(126, "🚔 QUI POURRAIT ?\nQui pourrait se faire bar'ban (arrêter) ?\nLa personne désignée boit 2 gorgées.", CardType.CHANCE),

        // Écart d'âge
        Card(127, "🔞 MILF / COUGAR\nCalcule l'écart d'âge max entre 2 personnes que tu as ken.\nBois ce nombre (0 = CUL SEC).", CardType.CHANCE),

        // Monopole
        Card(128, "🏘️ OPA HOSTILE\nSi tu possèdes 2/3 d'une famille, tu peux voler la propriété manquante pour 3 gorgées.", CardType.CHANCE),

        // Prénoms atypiques
        Card(129, "📛 PRÉNOMS ATYPIQUES\nTour de table : Citez les prénoms les plus chelous que vous avez pécho.\nLe pire donne 5 gorgées.", CardType.CHANCE),

        // Rouge ou Noir (x2)
        Card(130, "♦️ ROUGE OU NOIR ♠️\nTire une carte (virtuelle).\nRouge = Tu donnes 3.\nNoir = Tu bois 3.", CardType.CHANCE),
        Card(131, "♦️ ROUGE OU NOIR ♠️ (Têtes = Sec)\nTire une carte.\nRouge = Tu donnes le chiffre.\nNoir = Tu bois le chiffre.\nTête = CUL SEC.", CardType.CHANCE),

        // Hontes (Sexe / Alcool)
        Card(132, "😳 HONTE SEXE\nRaconte ta fois la plus honteuse (sexe) ou bois 5.", CardType.CHANCE),
        Card(133, "🤮 HONTE ALCOOL\nRaconte ta pire cuite ou bois 5.", CardType.CHANCE),

        // Échange
        Card(134, "🔄 VIS MA VIE\nÉchange ta place avec la personne en face de toi.", CardType.CHANCE),

        // Vol Porte d'Aix
        Card(135, "🧥 PORTE D'AIX\nTu te fais dépouiller porte d'Aix.\nEnlève 1 vêtement.", CardType.CHANCE),

        // Rejoue (x2)
        Card(136, "🎲 REJOUE\nC'est ton jour de chance.", CardType.CHANCE),
        Card(137, "🎲 REJOUE\nEncore une fois !", CardType.CHANCE),

        // Hasard
        Card(138, "⚖️ ÉQUILIBRE\nBois entre 1 et SEC. Distribue le même montant.", CardType.CHANCE),
        Card(139, "⚖️ KARMA\nBois entre 1 et SEC. Distribue le même montant.", CardType.CHANCE),

        // Vol payant
        Card(140, "💰 EXPROPRIATION\nVole la propriété de ton choix.\nCoût : Le prix de la case (en gorgées).", CardType.CHANCE),

        // Résolutions
        Card(141, "📅 BONNE ANNÉE\nNouvelles résolutions : Va à la Cave Départ.\nDonne 10 gorgées.", CardType.CHANCE),

        // Endormi improbable
        Card(142, "💤 NARCOLEPTIQUE\nQui pourrait s'endormir n'importe où ?\nElle raconte une anecdote ou boit 5.", CardType.CHANCE),

        // Space Cake
        Card(143, "🍪 SPACE CAKE DU WEI\nTu es défoncé.\nFais tes 2 prochains tours en reculant sur le plateau.", CardType.CHANCE),

        // Strip
        Card(144, "👙 STRIP-TEASE\nChoisis une personne. Elle enlève 1 vêtement.", CardType.CHANCE),

        // Mercredi (Barbu -> LTB)
        Card(145, "📅 C'EST MERCREDI\nVa au Bar'bu (Case 16) puis en after au LTB (Case 26).\nSi quelqu'un les possède, tu paies le loyer. Tu ne peux pas acheter.", CardType.CHANCE),

        // Date Elisa (Saint-Amour)
        Card(146, "🌹 DATE FOIREUX\nPiégé en date avec Elisa.\nTu lui achètes du Saint-Amour. Va case 38.", CardType.CHANCE),

        // Tour Daron/Daronne
        Card(147, "👨‍👩‍👦 TOUR DARONS\nChacun dit le nom de ses parents.\nSi quelqu'un a chopé un homonyme : elle boit 4.\nSi quelqu'un a baisé un homonyme : elle boit 8.\n(Cumulable !)", CardType.CHANCE),

        // Tour Frères/Soeurs
        Card(148, "👫 TOUR FRATRIE\nChacun dit le nom de ses frères/sœurs.\nSi quelqu'un a chopé : elle boit 2.\nSi quelqu'un a baisé : elle boit 4.\n(Cumulable !)", CardType.CHANCE),

        // Genoux
        Card(149, "🦵 CÂLIN (Gauche)\nPasse le prochain tour sur les genoux de ton voisin de GAUCHE.", CardType.CHANCE),
        Card(150, "🦵 CÂLIN (Droite)\nPasse le prochain tour sur les genoux de ton voisin de DROITE.", CardType.CHANCE),

        // Téléphone
        Card(151, "📱 LEAKS\nPasse ton téléphone à qui tu veux.\nIl scrolle ta galerie et montre 1 photo à tout le monde.\nRefus = CUL SEC.", CardType.CHANCE)
    )
}