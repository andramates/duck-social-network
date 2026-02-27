package service;

import domain.Friendship;
import domain.user.User;
import repository.Repository;

import java.util.*;

public class CommunityService {

    private final Repository<Long, User> userRepository;
    private final Repository<Long, Friendship> friendshipRepository;

    public CommunityService(Repository<Long, User> userRepository,
                            Repository<Long, Friendship> friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Calculeaza numarul de comunitati (componente conexe) din network.
     * Fiecare comunitate reprezinta un grup de utilizatori conectati direct sau indirect prin prietenii.
     * Se face DFS pentru identificarea componentelor
     *
     * @return numarul total de comunitati din retea
     */
    public int numberOfCommunities() {
        Map<Long, Set<Long>> adjacency = buildGraph();
        Set<Long> visited = new HashSet<>();
        int communityCount = 0;

        // pentru fiecare utilizator, daca nu a fost vizitat, pornim o noua componenta (DFS)
        for (User user : userRepository.findAll()) {
            long userId = user.getId();

            if (visited.add(userId)) {
                dfs(userId, adjacency, visited);
                communityCount++;
            }
        }

        return communityCount;
    }


    /**
     * Gaseste comunitatea cea mai sociabila, adica componenta conexa
     * care are diametrul maxim (cel mai lung drum minim intre doua noduri).
     *
     * @return lista de utilizatori care fac parte din cea mai sociabila comunitate
     */
    public List<User> mostSociableCommunity() {
        Map<Long, Set<Long>> adjacency = buildGraph();
        Set<Long> visited = new HashSet<>();
        Set<Long> bestComponent = Collections.emptySet();
        int bestDiameter = -1;

        // parcurgem toti utilizatorii si determinam fiecare componenta conexa
        for (User user : userRepository.findAll()) {
            long startId = user.getId();

            if (visited.add(startId)) {
                Set<Long> component = collectComponent(startId, adjacency, visited);
                int diameter = computeDiameter(component, adjacency);

                // pastram componenta cu diametrul cel mai mare
                if (diameter > bestDiameter) {
                    bestDiameter = diameter;
                    bestComponent = component;
                }
            }
        }

        // transformam id-urile in obiecte User si sortam dupa id
        List<User> mostSociable = new ArrayList<>();
        for (Long id : bestComponent) {
            User u = userRepository.findById(id);
            if (u != null) {
                mostSociable.add(u);
            }
        }
        mostSociable.sort(Comparator.comparing(User::getId));

        return mostSociable;
    }



    /**
     * Gaseste toate comunitatile (componentele conexe) din network
     * Fiecare comunitate este reprezentata ca o lista de obiecte User.
     *
     * @return o lista de comunitati, fiecare comunitate fiind o lista de utilizatori
     */
    public List<List<User>> findAllCommunities() {
        Map<Long, Set<Long>> adjacency = buildGraph();
        Set<Long> visited = new HashSet<>();
        List<List<User>> communities = new ArrayList<>();

        // pentru fiecare utilizator, daca nu a fost vizitat, pornim o noua componenta
        for (User user : userRepository.findAll()) {
            long userId = user.getId();

            if (visited.add(userId)) {
                // colectam toti utilizatorii care fac parte din aceeasi componenta
                Set<Long> componentIds = collectComponent(userId, adjacency, visited);
                List<User> component = new ArrayList<>();

                // mapam id-urile la obiectele User corespunzatoare
                for (Long id : componentIds) {
                    User foundUser = userRepository.findById(id);
                    if (foundUser != null) {
                        component.add(foundUser);
                    }
                }

                // sortam utilizatorii din componenta dupa id
                component.sort(Comparator.comparing(User::getId));
                communities.add(component);
            }
        }

        return communities;
    }



    /**
     * Construieste lista de adiacenta a networkului
     * Nodurile sunt utilizatori, iar muchiile sunt relatiile de prietenie dintre acestia.
     *
     * @return un map care asociaza fiecarui id de utilizator multimea id-urilor vecinilor sai
     */
    private Map<Long, Set<Long>> buildGraph() {
        Map<Long, Set<Long>> adjacency = new HashMap<>();

        // initializam fiecare utilizator cu o lista goala de vecini
        for (User user : userRepository.findAll()) {
            adjacency.put(user.getId(), new HashSet<>());
        }

        // adaugam muchiile bidirectionale pentru fiecare prietenie
        for (Friendship friendship : friendshipRepository.findAll()) {
            long userA = friendship.getUser1().getId();
            long userB = friendship.getUser2().getId();

            adjacency.computeIfAbsent(userA, k -> new HashSet<>()).add(userB);
            adjacency.computeIfAbsent(userB, k -> new HashSet<>()).add(userA);
        }

        return adjacency;
    }


    /**
     * DFS
     *
     * @param startId   id-ul nodului de pornire
     * @param adjacency lista de adiacenta a grafului
     * @param visited   multimea nodurilor deja vizitate
     */
    private void dfs(long startId, Map<Long, Set<Long>> adjacency, Set<Long> visited) {
        ArrayDeque<Long> stack = new ArrayDeque<>();
        stack.push(startId);

        while (!stack.isEmpty()) {
            long current = stack.pop();
            for (long neighbor : adjacency.getOrDefault(current, Set.of())) {
                // daca vecinul nu a fost vizitat, il marcam si il adaugam in stiva
                if (visited.add(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
    }


    /**
     * Colecteaza toate nodurile care fac parte din aceeasi componenta conexa cu nodul de start.
     * Se face DFS pentru a aduna toate nodurile accesibile.
     *
     * @param startId     id-ul nodului de pornire
     * @param adjacency   lista de adiacenta a grafului
     * @param visitedAll  multimea nodurilor deja vizitate global
     * @return multimea id-urilor care apartin componentei conexe curente
     */
    private Set<Long> collectComponent(long startId, Map<Long, Set<Long>> adjacency, Set<Long> visitedAll) {
        Set<Long> component = new HashSet<>();
        ArrayDeque<Long> stack = new ArrayDeque<>();

        stack.push(startId);
        component.add(startId);

        while (!stack.isEmpty()) {
            long current = stack.pop();
            for (long neighbor : adjacency.getOrDefault(current, Set.of())) {
                // daca nodul vecin nu a fost vizitat inca, il adaugam in componenta si in stiva
                if (visitedAll.add(neighbor)) {
                    component.add(neighbor);
                    stack.push(neighbor);
                } else {
                    // daca a fost deja vizitat, il adaugam doar in componenta
                    component.add(neighbor);
                }
            }
        }
        return component;
    }


    /**
     * Calculeaza diametrul unei componente conexe.
     * Diametrul = cea mai mare distanta minima intre doua noduri din componenta.
     * Pentru fiecare nod din componenta se face un BFS și se retine distanta maxima
     *
     * @param component multimea ID-urilor din componenta curenta
     * @param adjacency lista de adiacenta a grafului (neorientat)
     * @return diametrul componentei (lungimea celui mai lung drum minim)
     */
    private int computeDiameter(Set<Long> component, Map<Long, Set<Long>> adjacency) {
        int diameter = 0;

        for (long sourceId : component) {
            int maxDistance = maxDistanceFrom(sourceId, component, adjacency);
            diameter = Math.max(diameter, maxDistance);
        }

        return diameter;
    }

    /**
     * Calculeaza distanta maxima de la un nod sursa intr-o componenta (folosind BFS).
     * @param sourceId ID-ul nodului de pornire
     * @param component mulțimea ID-urilor din componenta curenta
     * @param adjacency lista de adiacenta a grafului
     * @return distanta maxima (in nr de muchii) catre orice alt nod din componenta
     */
    private int maxDistanceFrom(long sourceId, Set<Long> component, Map<Long, Set<Long>> adjacency) {
        Map<Long, Integer> distance = new HashMap<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();

        queue.add(sourceId);
        distance.put(sourceId, 0);

        int maxDistance = 0;

        while (!queue.isEmpty()) {
            long current = queue.poll();
            int currentDist = distance.get(current);

            for (long neighbor : adjacency.getOrDefault(current, Set.of())) {
                // procesam doar vecinii care fac parte din componenta curenta și nu au fost vizitati
                if (component.contains(neighbor) && !distance.containsKey(neighbor)) {
                    int neighborDist = currentDist + 1;
                    distance.put(neighbor, neighborDist);
                    maxDistance = Math.max(maxDistance, neighborDist);
                    queue.add(neighbor);
                }
            }
        }

        return maxDistance;
    }

}
