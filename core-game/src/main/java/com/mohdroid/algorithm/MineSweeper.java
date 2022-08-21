package com.mohdroid.algorithm;

import static com.mohdroid.algorithm.DirectionalOfMovement.EAST;
import static com.mohdroid.algorithm.DirectionalOfMovement.NORTH;
import static com.mohdroid.algorithm.DirectionalOfMovement.SOUTH;
import static com.mohdroid.algorithm.DirectionalOfMovement.WEST;

import com.mohdroid.algorithm.dto.AssetDetailResponse;
import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.ItemCreator;
import com.mohdroid.algorithm.item.ItemType;
import com.mohdroid.algorithm.item.assets.ExitAsset;
import com.mohdroid.algorithm.item.assets.StartAsset;
import com.mohdroid.algorithm.item.assets.StepAsset;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


public class MineSweeper {

    public int col, row, n;
    Node soldier;
    int sr, sc, er, ec;
    private Node[][] gameBoard;

    public MineSweeper(int col, int row, List<ItemDetail> items) {
        this.col = col;
        this.row = row;
        n = row * col;
        gameBoard = new Node[row][col];
        initEmptyBoard();
        indicatePredefineItems(items);
        indicateShortestPath(items);
        indicateStartNeighbours(items);
        indicateBlocks(items);
        indicateAccessibleItems(items);
        indicateOthers(items);
    }

    private void initEmptyBoard() {
        int count = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                gameBoard[i][j] = new Node(null, i, j, count++, null);
            }
        }
    }

    private void indicatePredefineItems(List<ItemDetail> items) {
        Iterator<ItemDetail> iterator = items.iterator();
        while (iterator.hasNext()) {
            ItemDetail item = iterator.next();
            if (item.getPosition() != null) {
                gameBoard[item.getPosition().getX()][item.getPosition().getY()].item = item.getItem();
                if (item.getItem() instanceof StartAsset) {
                    sr = item.getPosition().getX();
                    sc = item.getPosition().getY();
                } else if (item.getItem() instanceof ExitAsset) {
                    er = item.getPosition().getX();
                    ec = item.getPosition().getY();
                }
                iterator.remove();
            }
        }
    }

    private void indicateStartNeighbours(List<ItemDetail> items) {
         /*

            N.W   N   N.E
              \   |   /
               \  |  /
            W----Cell----E
                 / | \
               /   |  \
            S.W    S   S.E

        Cell-->Current Cell (row, col)
        N -->  North        (row-1, col)
        S -->  South        (row+1, col)
        E -->  East         (row, col+1)
        W -->  West            (row, col-1)
        N.E--> North-East   (row-1, col+1)
        N.W--> North-West   (row-1, col-1)
        S.E--> South-East   (row+1, col+1)
        S.W--> South-West   (row+1, col-1)
    */
        //----------- 1st Neighbour (North) ------------
        if (isValid(sr - 1, sc))
            if (isEmpty(sr - 1, sc))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr - 1][sc].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }

        //----------- 2nd Neighbour (South) ------------
        if (isValid(sr + 1, sc))
            if (isEmpty(sr + 1, sc))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr + 1][sc].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 3rd Neighbour (East) ------------
        if (isValid(sr, sc + 1))
            if (isEmpty(sr, sc + 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr][sc + 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 4th Neighbour (West) ------------

        if (isValid(sr, sc - 1))
            if (isEmpty(sr, sc - 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr][sc - 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 5th Neighbour (North-East) ------------
        if (isValid(sr - 1, sc + 1))
            if (isEmpty(sr - 1, sc + 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr - 1][sc + 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 6th Neighbour (North-West) ------------
        if (isValid(sr - 1, sc - 1))
            if (isEmpty(sr - 1, sc - 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr - 1][sc - 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 7th Neighbour (South-East) ------------
        if (isValid(sr + 1, sc + 1))
            if (isEmpty(sr + 1, sc + 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr + 1][sc + 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
        //----------- 8th Neighbour (South-West) ------------
        if (isValid(sr + 1, sc - 1))
            if (isEmpty(sr + 1, sc - 1))
                for (ItemDetail itemDetail : items)
                    if (itemDetail.getItem() instanceof StepAsset) {
                        gameBoard[sr + 1][sc - 1].item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }

    }

    private void indicateBlocks(List<ItemDetail> items) {
        List<Node> nodes = findEmptyNodes();
        List<ItemDetail> blocks = new ArrayList<>();
        for (ItemDetail item : items) {
            if (item.getItem().isBlock())
                blocks.add(item);
        }
        removeItems(items, blocks);
        Iterator<ItemDetail> iterator = blocks.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            nodes.get(count++).item = iterator.next().getItem();
            iterator.remove();
        }
    }

    private void indicateAccessibleItems(List<ItemDetail> items) {
        List<Node> nodes = findEmptyNodes();
        List<ItemDetail> accesses = new ArrayList<>();
        for (ItemDetail item : items) {
            if (item.getItem().isShouldAccessible()) {
                accesses.add(item);
            }
        }
        removeItems(items, accesses);
        Iterator<ItemDetail> iterator = accesses.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next().getItem();
            for (Node node : nodes) {
                List<Node> path = reconstructPath(sr, sc, node.x, node.y);
                if (path != null) {
                    formatPath(path);
                    node.item = item;
                    iterator.remove();
                    nodes.remove(node);
                    break;
                }
            }
        }
        if (accesses.isEmpty()) {
            return;
        }
        nodes.clear();
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                if (gameBoard[i][j].item instanceof StepAsset)
                    nodes.add(gameBoard[i][j]);
        Collections.shuffle(nodes);
        iterator = accesses.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next().getItem();
            for (Node node : nodes) {
                List<Node> path = reconstructPath(sr, sc, node.x, node.y);
                if (path != null) {
                    formatPath(path);
                    items.add(new ItemDetail(node.item, new AssetDetailResponse.Position(node.x, node.y)));
                    node.item = item;
                    iterator.remove();
                    nodes.remove(node);
                    break;
                }
            }
        }

    }

    private void indicateShortestPath(List<ItemDetail> items) {
        List<Node> paths = reconstructPath(sr, sc, er, ec);
        if (paths == null)
            throw new IllegalStateException();
        for (Node node : paths) {
            if (node.item == null) {
                for (ItemDetail itemDetail : items) {
                    if (itemDetail.getItem() instanceof StepAsset) {
                        node.item = itemDetail.getItem();
                        items.remove(itemDetail);
                        break;
                    }
                }
            }
        }
    }

    private List<Node> findEmptyNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (gameBoard[i][j].item == null) {
                    nodes.add(gameBoard[i][j]);
                }
            }
        }
        Collections.shuffle(nodes);
        return nodes;
    }

    private static String formatPath(List<Node> path) {
        List<Integer> integers = new ArrayList<>();
        path.forEach(node -> integers.add(node.label));
        return integers.stream().map(Object::toString).collect(Collectors.joining(" -> "));
    }

    private void indicateOthers(List<ItemDetail> items) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (gameBoard[i][j].item == null)
                    if (items.isEmpty())
                        gameBoard[i][j].item = ItemCreator.createItem(ItemType.STEP);
                    else
                        gameBoard[i][j].item = items.remove(0).getItem();
            }
        }
    }

    private List<Node> reconstructPath(int sr, int sc, int er, int ec) {
        Node start = gameBoard[sr][sc];
        Node end = gameBoard[er][ec];
        Node[] prev = bfs(start, end);
        List<Node> path = new ArrayList<>();
        for (Node at = end; at != null; at = prev[at.label])
            path.add(at);
        Collections.reverse(path);
        if (path.get(0) == start) return path;
        return null;
    }

    private Node[] bfs(Node startNode, Node end) {
        Node[] prev = new Node[n];
        boolean[][] visited = new boolean[row][col];
        Deque<Node> queue = new ArrayDeque<>(n);
        //start by visiting the start cell and add it to the queue.
        queue.offer(startNode);
        visited[sr][sc] = true;
        //continue until the BFS done
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            List<Node> neighbours = findNeighbours(node.x, node.y);
            // Loop through all edges attached to this node. Mark nodes as visited once they're
            // in the queue. This will prevent having duplicate nodes in the queue and speedup the BFS.
            for (Node next : neighbours) {
                if (!visited[next.x][next.y]) {
                    if (next.item != null && next.item.isBlock())
                        if (!(next == end)) continue;
                    visited[next.x][next.y] = true;
                    prev[next.label] = node;
                    queue.offer(next);
                }
            }
        }
        return prev;
    }

    /*
        utility function to find neighbour cell of Node, and return random order of nodes,
     */
    private List<Node> findNeighbours(int r, int c) {
        DIR[] dirs = DIR.values();
        Collections.shuffle(Arrays.asList(dirs));
        List<Node> neighbours = new ArrayList<>(4);
        for (DIR dir : dirs) {
            int rr = r + dir.dx;
            int cc = c + dir.dy;
            if (between(rr, row) && between(cc, col)) {
                neighbours.add(gameBoard[rr][cc]);
            }
        }
        return neighbours;
    }

    private enum DIR {
        N(0, -1), S(0, 1), E(1, 0), W(-1, 0);

        private final int dx;
        private final int dy;

        private DIR(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Item item = gameBoard[i][j].item;
                char indicator = ' ';
                if (item != null) indicator = item.getIndicator();
                sb.append(indicator);
                sb.append(" | ");
            }
            sb.delete(sb.length() - 2, sb.length() - 1);
            sb.append('\n');
        }
        return sb.toString();
    }

    public Node[][] getGameBoard() {
        return gameBoard;
    }

    public void update(Node node, Item item) {
        node.prev = node;
        node.item = item;
    }

    public Node checkToPlay(DirectionalOfMovement directionalOfMovement) throws IllegalArgumentException {
        /*
                            N
                            |
                            |
                      W----Cell----E
                            |
                            |
                            S

                  Cell-->Current Cell (row, col)
                  N -->  North        (row-1, col)
                  S -->  South        (row+1, col)
                  E -->  East         (row, col+1)
                  W -->  West            (row, col-1)
         */

        //----------- 1st Neighbour (North) ------------
        if (soldier == null)
            soldier = new Node(ItemCreator.createItem(ItemType.SOLDIER), sr, sc, n, gameBoard[sr][sc]);
        int tempX = 0, tempY = 0;
        if (directionalOfMovement == NORTH) {
            tempX = soldier.x - 1;
            tempY = soldier.y;
        } else if (directionalOfMovement == SOUTH) {
            tempX = soldier.x + 1;
            tempY = soldier.y;
        } else if (directionalOfMovement == EAST) {
            tempX = soldier.x;
            tempY = soldier.y + 1;
        } else if (directionalOfMovement == WEST) {
            tempX = soldier.x;
            tempY = soldier.y - 1;
        }
        if (isValid(tempX, tempY))
            return gameBoard[tempX][tempY];
        return null;

    }

    public void play(Node node) {
        gameBoard[soldier.x][soldier.y] = soldier.prev;
        gameBoard[node.x][node.y] = soldier;
        soldier.x = node.x;
        soldier.y = node.y;
        soldier.prev = node;
    }

    public static class Node {
        public Item item;
        int x, y, label;
        public Node prev;

        public Node(Item item, int x, int y, int label, Node prev) {
            this.item = item;
            this.x = x;
            this.y = y;
            this.label = label;
            this.prev = prev;
        }
    }

    // A Utility Function to check whether given cell (row, col) is a valid cell or not
    private boolean isValid(int row, int col) {
        return row >= 0 && row < this.row && col >= 0 && col < this.col;
    }

    private static boolean between(int v, int upper) {
        return (v >= 0) && (v < upper);
    }

    // A Utility Function to check whether given cell (row, col) has a mine or not
    private boolean isEmpty(int row, int col) {
        return gameBoard[row][col].item == null;
    }

    private void removeItems(List<ItemDetail> items, List<ItemDetail> removeItems) {
        for (ItemDetail itemDetail : removeItems)
            items.remove(itemDetail);
    }

}
