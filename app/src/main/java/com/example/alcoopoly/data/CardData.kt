package com.example.alcoopoly.data

import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card

object CardData {

    // --- MINI-JEUX (ID 200+) ---
    val initialMiniGameCards = listOf(
        Card(201, "👀 BATAILLE DE REGARD\nChoisis une personne. Le premier qui cligne des yeux ou détourne le regard boit 5 gorgées.", CardType.MINI_JEU),
        Card(202, "🧳 DANS MA VALISE\nLe joueur commence : \"Dans ma valise il y a...\". Le suivant répète et ajoute un objet. Le premier qui se trompe boit 3.", CardType.MINI_JEU),
        Card(203, "👉 QUI POURRAIT ?\nLance un \"Qui pourrait...\". À 3, tout le monde pointe quelqu'un. La personne visée boit le nombre de votes reçus.", CardType.MINI_JEU),
        Card(204, "📱 1024\nTout le monde prend son tel en mode calculatrice et tape un nombre entre 1 et 1024. Si deux personnes ont le même nombre = Ils boivent 2. Si quelqu'un met 1 ou 1024 seul = Il distribue 2. Ensuite on réduit (1-512, etc.).", CardType.MINI_JEU),
        Card(205, "🐱 BITE - CHATTE - COUILLES\nComptez en chiffres romains (1 symbole par personne). Bite = I, Chatte = V, Couilles = X. Faire 5 tours rapides.", CardType.MINI_JEU),
        Card(206, "🤥 3 ANECDOTES\nRaconte 2 vérités, 1 mensonge. Les autres votent pour le mensonge. Ceux qui se trompent boivent 5. Si personne ne se trompe : CUL SEC pour toi.", CardType.MINI_JEU),
        Card(207, "⚖️ TU PRÉFÈRES ?\nPose un “tu préfères ?”. La minorité boit le nombre de gorgées de la majorité. (Ex: 5 vs 2 -> Les 2 boivent 5).", CardType.MINI_JEU),
        Card(208, "🐎 PMU\nSortez 4 valets. Pariez des gorgées sur une couleur (buvez pour valider). 1er : Distribue double. 2e : Distribue mise. 3e : Boit mise. 4e : Boit double.", CardType.MINI_JEU),
        Card(209, "🪙 JEU DE LA PIÈCE\nVa poser une question dans l’oreille de qui tu veux, il doit répondre en donnant la pièce à la personne qu’il désigne comme sa réponse. La personne désignée connaîtra la question si elle réussit son pile ou face (sinon elle ne saura jamais pourquoi on l’a désignée).", CardType.MINI_JEU),
        Card(210, "🧪 JUSTE CUITE\nLe maître de maison te prépare un shot mystère (pur). Devine le degré et bois la différence (Ex: dit 40°, réel 45° -> 5 gorgées).", CardType.MINI_JEU),
        Card(211, "📝 RIME\nTu commences avec un mot. Tour de table, chacun doit rimer. Le premier qui sèche ou répète boit 3.", CardType.MINI_JEU),
        Card(212, "🔢 LE 99\nComptez jusqu'à 99 (Valet +/-10, Dame change sens, Roi = 70, As = 1 ou 11). Erreur ou dépassement = CUL SEC.", CardType.MINI_JEU),
        Card(213, "💡 CRÉATIF\nInvente un mini-jeu maintenant ou refais-en un que tu as kiffé.", CardType.MINI_JEU),
        Card(214, "😇 JE N'AI JAMAIS\nDis un \"Je n'ai jamais...\". Ceux qui l'ont fait boivent 2.", CardType.MINI_JEU),
        Card(215, "😇 J'AI DÉJÀ\nDis un \"J'ai déjà...\". Ceux qui NE l'ont PAS fait boivent 2.", CardType.MINI_JEU),
        Card(216, "💧 VOD'KEAU\nChoisis 2 joueurs pour t’accompagner. 1 shot de vodka, 2 d'eau. Buvez et pariez sur qui a la vodka. Les perdants boivent un shot.", CardType.MINI_JEU),
        Card(217, "🔤 THÈME\nChoisis un thème. Chacun cite un élément. Le premier qui sèche boit 3.", CardType.MINI_JEU),
        Card(218, "🌊 LA CASCADE\nTu commences à boire. Ton voisin suit. Quand tu arrêtes, ton voisin peut s'arrêter, et ainsi de suite.", CardType.MINI_JEU),
        Card(219, "🧠 QUESTION POUR UN CHAMPION\nPose une question de culture G. Le premier qui répond juste distribue 3. Si personne ne trouve, tu bois 3.", CardType.MINI_JEU),
        Card(220, "🔢 LE CHIFFRE MAUDIT\nChoisis un chiffre entre 1 et 6. Jusqu'au prochain tour, à chaque fois qu'on lance les dés pour n'importe quoi, si ce chiffre sort, tu bois.", CardType.MINI_JEU),
        Card(221, "🗿 MÉDUSA\nTout le monde baisse la tête. À 3, on relève la tête en regardant quelqu'un. Si deux personnes se regardent -> Elles boivent 3.", CardType.MINI_JEU)
    )

    // --- CARTES CHANCE (ID 100+) ---
    val initialChanceCards = listOf(
        // --- DÉPLACEMENTS & ACTIONS LOGIQUES (ID 101-110) ---
        Card(101, "📅 BONNE ANNÉE\nNouvelles résolutions : Va à la Cave Départ. Donne 10 gorgées.", CardType.CHANCE),
        Card(102, "🚔 ALCOOL AU VOLANT\nContrôle de police positif. Direction la cellule de dégrisement (Bar’ban) sans passer par la case départ.", CardType.CHANCE),
        Card(103, "🍺 C'EST MERCREDI\nVa au Bar'bu (Case 16). Si possédé : paie le loyer. Sinon : tu ne peux pas acheter, juste boire un coup.", CardType.CHANCE),
        Card(104, "🌹 DATE FOIREUX\nPiégé en date avec Elisa. Tu lui achètes du Saint-Amour. Va case 38.", CardType.CHANCE),
        Card(105, "😵 SOIRÉE BDE\nTu reprends conscience en fin de soirée BDE. Avance directement à la case 'Soirée BDE' (Case 36).", CardType.CHANCE),
        Card(106, "📱 OUBLI DE TEL\nTu as oublié ton téléphone au bar précédent... Recule jusqu’au bar le plus proche.", CardType.CHANCE),
        Card(107, "🍪 SPACE CAKE DU WEI\nLe gâteau était chargé. Tu es défoncé. Fais tes 2 prochains tours en reculant sur le plateau (lancer inversé).", CardType.CHANCE),
        Card(108, "🔄 VIS MA VIE\nÉchange ta place (et ton pion) avec la personne en face de toi.", CardType.CHANCE),

        // --- CARTES TEXTE (ID 120+) ---
        // Vols & Attaques
        Card(120, "👑 MAÎTRE DU LIMOUSIN\nBois 5 gorgées. En échange, tu deviens le Maître du Limousin (Tu peux l'activer quand tu veux).", CardType.CHANCE),
        Card(121, "🏴‍☠️ VOL DE PROPRIÉTÉ\nChoisis une victime. Lancez les dés. Si tu fais strictement plus qu'elle, vole-lui la propriété de ton choix, sinon bois l’écart aux dés. (Ex : 5 vs 9 : Tu bois 4).", CardType.CHANCE),
        Card(122, "💰 EXPROPRIATION\nVole la propriété de ton choix. Coût : Le prix de la case (en gorgées) à boire.", CardType.CHANCE),
        Card(123, "🏘️ OPA HOSTILE\nSi tu possèdes 2 cartes d'une famille, tu peux voler la 3ème manquante à un joueur pour 5 gorgées.", CardType.CHANCE),
        Card(124, "🎭 VOL D'IDENTITÉ\n(Très Rare) Échange ta place, tes propriétés et ton argent avec le joueur de ton choix.", CardType.CHANCE),
        Card(125, "👕 VOL À L'ÉTALAGE\nVole un vêtement à la personne de ton choix et mets-le.", CardType.CHANCE),
        Card(126, "🧥 PORTE D'AIX\nTu te fais dépouiller Porte d'Aix. Enlève 1 vêtement.", CardType.CHANCE),

        // Règles Temporaires
        Card(127, "🤐 NI OUI NI NON\nJusqu'à ton prochain tour, interdit de dire Oui ou Non. 1 gorgée par erreur.", CardType.CHANCE),
        Card(128, "👍 ROI DES POUCES\nTu es le roi des pouces. Quand tu poses ton pouce sur la table, le dernier à le faire boit 2. Valable 3 fois max.", CardType.CHANCE),
        Card(129, "👈 GAUCHE\nTu chopes le joueur à ta gauche. Pendant 1 tour : Si tu bois, il boit (et inversement).", CardType.CHANCE),
        Card(130, "👉 DROITE\nTu chopes le joueur à ta droite. Pendant 1 tour : Si tu bois, il boit (et inversement).", CardType.CHANCE),
        Card(131, "🤝 PARTENAIRE\nChoisis un partenaire. Pendant 1 tour, vous êtes liés : si l'un boit, l'autre aussi.", CardType.CHANCE),
        Card(132, "🦵 CÂLIN (Gauche)\nPasse le prochain tour assis sur les genoux de ton voisin de gauche.", CardType.CHANCE),
        Card(133, "🦵 CÂLIN (Droite)\nPasse le prochain tour assis sur les genoux de ton voisin de droite.", CardType.CHANCE),

        // Défis & Hasard
        Card(134, "👊 CHI-FOU-BOIS\nDésigne un adversaire. Chi-Fou-Bois en 1 manche gagnante. Le perdant boit 3.", CardType.CHANCE),
        Card(135, "♦️ ROUGE OU NOIR\nTire une carte. Rouge = Tu donnes 3. Noir = Tu bois 3.", CardType.CHANCE),
        Card(136, "🏃 DEALER\nTu te fais courser par un dealer d’en bas. Lance les dés jusqu'à faire un double pour le semer. Bois 2 gorgées par essai raté.", CardType.CHANCE),
        Card(137, "🤔 POUR COMBIEN ?\nLance un \"Pour combien ?\" à la personne en face de toi. (Reverse /2).", CardType.CHANCE),
        Card(138, "😈 ACTION OU VÉRITÉ\nLance un “action ou vérité” à qui tu veux. S’il refuse = CUL SEC.", CardType.CHANCE),
        Card(139, "🎲 REJOUE\nC'est ton jour de chance. Relance les dés immédiatement.", CardType.CHANCE),

        // Distributions & Gorgées
        Card(140, "🎁 CADEAU\nDonne 4 gorgées à qui tu veux.", CardType.CHANCE),
        Card(141, "🏙️ MAGNAT\nDonne 4 gorgées à celui qui a le plus de propriétés.", CardType.CHANCE),
        Card(142, "⛺ SDF\nDonne 4 gorgées à celui qui a le moins de propriétés.", CardType.CHANCE),
        Card(143, "🎂 ANNIVERSAIRE\nC'est ton anniversaire (ou pas). CUL SEC !", CardType.CHANCE),
        Card(144, "🍻 SANTÉ\nTout le monde boit 1 gorgée.", CardType.CHANCE),
        Card(145, "🥂 À LA TIENNE\nTout le monde boit 2 gorgées en ton honneur.", CardType.CHANCE),
        Card(146, "💸 IMPÔTS\nBois 1 gorgée par propriété que tu possèdes.", CardType.CHANCE),
        Card(147, "🏦 ERREUR BANCAIRE\nLa banque se trompe. Distribue 5 gorgées.", CardType.CHANCE),
        Card(148, "⚖️ KARMA\nBois entre 1 et 10 gorgées. Distribue exactement le même montant.", CardType.CHANCE),
        Card(149, "🌿 AFTER FARIGOULE\nTu termines en after farigoule et bois la bassine de ton plein gré (bois 3 de ton verre si elle est vide).", CardType.CHANCE),
        Card(150, "♠️ COUP DE POKER\nPioche une carte.\nNoir = Bois le chiffre.\nRouge = Donne le chiffre.\nTête = CUL SEC.", CardType.CHANCE),

        // Anecdotes
        Card(151, "😳 HONTE SEXE\nRaconte ta pire honte sexuelle ou bois 5.", CardType.CHANCE),
        Card(152, "🤮 HONTE ALCOOL\nRaconte ta pire cuite ou bois 5.", CardType.CHANCE),
        Card(153, "💘 COUPLE\nQui aurait le plus de chance de finir ensemble ici ? Choisis 2 personnes. Elles boivent 2.", CardType.CHANCE),
        Card(154, "🥴 SAM\nQui a le plus de chance de finir honteux ce soir ? Il boit 3 gorgées.", CardType.CHANCE),
        Card(155, "📱 LEAKS\nPasse ton tel à qui tu veux. Il peut scroller ta galerie et montrer la photo de son choix au groupe. Si tu refuses = CUL SEC.", CardType.CHANCE),
        Card(156, "🔞 MILF/COUGAR\nCalcule l'écart d'âge max entre 2 personnes que tu as pécho. Bois ce nombre.", CardType.CHANCE),
        Card(157, "👨‍👩‍👦 TOUR FRERES/SOEURS\nCitez les prénoms de vos frères et soeurs. Homonyme chopé = 2 gorgées. Homonyme baisé = 4 gorgées. (cumulable)", CardType.CHANCE),
        Card(158, "👨‍👩‍👦 TOUR DARONS\nCitez les prénoms de vos parents. Homonyme chopé = 4 gorgées. Homonyme baisé = 8 gorgées. (cumulable)", CardType.CHANCE),
        Card(159, "📛 PRÉNOMS ATYPIQUES\nTour de table : Quels sont les 3 prénoms les plus atypiques que t'as chopé ? La personne avec les prénoms les plus atypiques donne 5.", CardType.CHANCE),
        Card(160, "💤 NARCOLEPTIQUE\nQui pourrait s'endormir par terre ici même ? Votez tous à 3. La personne désignée boit le nombre de votes qu’elle a reçus.", CardType.CHANCE)
    )
}