package com.wtech.fitness.models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RedBlackTreeTests {

    private var tree: RedBlackTree<Int, Int> = buildTree()
    private val nodes: List<Node<Int, Int>> = tree.getNodes()

    private val <T: Comparable<T>, R> Node<T, R>.color get(): NodeColor = (this as? RedBlackNode)?.color ?: NodeColor.BLACK

    private val `5` = nodes.find { (it as? RedBlackNode)?.key == 5 }
    private val `10` = nodes.find { (it as? RedBlackNode)?.key == 10 }
    private val `8` = nodes.find { (it as? RedBlackNode)?.key == 8 }
    private val `2` = nodes.find { (it as? RedBlackNode)?.key == 2 }
    private val `12` = nodes.find { (it as? RedBlackNode)?.key == 12 }
    private val `6` = nodes.find { (it as? RedBlackNode)?.key == 6 }
    private val `9` = nodes.find { (it as? RedBlackNode)?.key == 9 }
    private val root5State: () -> Boolean
        get() = {
                `5` != null &&
                `5`.parent is NilNode &&

                `5`.leftChild == `2` &&
                `5`.rightChild == `10` &&

                `10`.parent == `5` &&
                `10`.leftChild == `8` &&
                `10`.rightChild == `12` &&

                `8`.parent == `10` &&
                `8`.leftChild == `6` &&
                `8`.rightChild == `9` &&

                `2`.parent == `5` &&
                `2`.leftChild is NilNode &&
                `2`.rightChild is NilNode &&

                `12`.parent == `10` &&
                `12`.leftChild is NilNode &&
                `12`.rightChild is NilNode &&

                `6`.parent == `8` &&
                `6`.leftChild is NilNode &&
                `6`.rightChild is NilNode &&

                `9`.parent == `8` &&
                `9`.leftChild is NilNode &&
                `9`.rightChild is NilNode
    }

    private val root10State = {
        `10`?.parent is NilNode &&
                `10`.leftChild == `5` &&
                `10`.rightChild == `12` &&

                `5`.parent == `10` &&
                `5`.leftChild == `2` &&
                `5`.rightChild == `8` &&

                `8`.parent == `5` &&
                `8`.leftChild == `6` &&
                `8`.rightChild == `9` &&

                `12`.parent == `10` &&
                `12`.leftChild is NilNode &&
                `12`.rightChild is NilNode &&

                `2`.parent == `5` &&
                `2`.leftChild is NilNode &&
                `2`.rightChild is NilNode &&

                `6`.parent == `8` &&
                `6`.leftChild is NilNode &&
                `6`.rightChild is NilNode &&

                `9`.parent == `8` &&
                `9`.leftChild is NilNode &&
                `9`.rightChild is NilNode
    }

    private fun buildTree(): RedBlackTree<Int, Int> {
        return RedBlackTree<Int, Int>()
            .apply {
                insert(RedBlackNode(5, 5))
                insert(RedBlackNode( 2, 2))
                insert(RedBlackNode(10, 10))
                insert(RedBlackNode(8, 8))
                insert(RedBlackNode(12, 12))
                insert(RedBlackNode(6,6 ))
                insert(RedBlackNode(9, 9))
            }
    }

    @Test
    fun `insertions should generate root5State`() {
        buildTree()
        assert(root5State())
    }

    @Test fun `test insertion coloring`() {
        val tree = RedBlackTree<Int, Int>()

        val `5` = RedBlackNode(5, 5)
        assert(`5`.color == NodeColor.RED)

        tree.insert(`5`)
        assert(`5`.color == NodeColor.BLACK)

        val `2` = RedBlackNode(2, 2)
        assert(
            `2`.color == NodeColor.RED &&
                    `5`.color == NodeColor.BLACK
        )

        tree.insert(`2`)
        assert(
            `2`.color == NodeColor.RED
                    && `5`.color == NodeColor.BLACK
        )

        val `10` = RedBlackNode(10, 10)
        assert(
            `10`.color == NodeColor.RED &&
                    `2`.color == NodeColor.RED &&
                    `5`.color == NodeColor.BLACK
        )

        tree.insert(`10`)
        assert(
            `10`.color == NodeColor.RED &&
                    `2`.color == NodeColor.RED &&
                    `5`.color == NodeColor.BLACK
        )

        val `8` = RedBlackNode(8, 8)
        assert(
            `8`.color == NodeColor.RED &&
                    `10`.color == NodeColor.RED &&
                    `2`.color == NodeColor.RED &&
                    `5`.color == NodeColor.BLACK
        )

        tree.insert(`8`)
        assert(
            `8`.color == NodeColor.RED &&
                    `10`.color == NodeColor.BLACK &&
                    `2`.color == NodeColor.BLACK &&
                    `5`.color == NodeColor.BLACK
        ) {
            """
                8: ${`8`.color} should be ${NodeColor.RED}
                10: ${`10`.color} should be ${NodeColor.BLACK}
                2: ${`2`.color} should be ${NodeColor.BLACK}
                5: ${`5`.color} should be ${NodeColor.BLACK}
            """.trimIndent()
        }


    }

    @Test
    fun `left rotation`() {
        buildTree()
        (`5` as RedBlackNode).rotate(RedBlackNode.Direction.LEFT)
        assert(root10State())
    }

    @Test
    fun `right rotation`() {
        buildTree()
        (`5` as RedBlackNode).rotate(RedBlackNode.Direction.LEFT)
        (`10` as RedBlackNode).rotate(RedBlackNode.Direction.RIGHT)
        assert(root5State())
    }

    @Test
    fun `get all nodes`() {
        val expected: Set<Node<Int, Int>?> = setOf(`5`, `10`, `8`, `2`, `12`, `6`, `9`)
        assertEquals(expected.first(), buildTree().getNodes().first())
        assertEquals(expected, buildTree().getNodes().filter { it !is NilNode }.toSet())
    }

    @Test
    fun `test search`() {
        val tree = buildTree()
        val expected = RedBlackNode(20, 20)
        tree.insert(expected)

        assertEquals(expected, tree.find(expected.key))
    }

    @Test
    fun `all nodes in tree are either red or black`() {
        val tree = buildTree()
        assert(tree.getNodes().all { it.color in NodeColor.values() })
    }

    @Test
    fun `leaf nodes are nodes with no children`() {
        val tree = buildTree()
        val leaves: List<Node<Int, Int>> = tree.getLeaves()
        assert(leaves.isNotEmpty())
        assert(
            leaves.all {
                it is NilNode || (it.leftChild is NilNode && it.rightChild is NilNode)
            }
        )
    }

    @Test
    fun `internal nodes are nodes with at least one child`() {
        val tree = buildTree()
        val internalNodes: List<Node<Int, Int>> = tree.getNodes() - tree.getLeaves().toSet()
        assert(internalNodes.isNotEmpty())
        assert(internalNodes.none { it.leftChild is NilNode || it.rightChild is NilNode })
    }

    @Test
    fun `root and leaves are black`() {
        val tree = buildTree()
        val root: Node<Int, Int> = tree.root

        assert((root as? RedBlackNode)?.color == NodeColor.BLACK)
    }

    @Test
    fun `if a node is red, its children are black`() {
        val tree = buildTree()
        val redNodes: List<Node<Int, Int>> = tree.getNodes()
            .filter { (it as? RedBlackNode)?.color == NodeColor.RED }

        assert(
            redNodes.all { node ->
                (node.leftChild is NilNode || (node.leftChild as? RedBlackNode)?.color == NodeColor.BLACK)
                        && (node.rightChild is NilNode || (node.rightChild as? RedBlackNode)?.color == NodeColor.BLACK)
            })
    }

    @Test
    fun `all paths from a node to its NIL descendents contain the same # of black nodes`() {
        val tree = buildTree()
        val nodesWithAtLeastOneNilChild: List<RedBlackNode<Int, Int>> = tree.getNodes()
            .filter { it.leftChild is NilNode || it.rightChild is NilNode }
            .mapNotNull { it as? RedBlackNode }

        val paths: List<List<Node<Int, Int>>> = nodesWithAtLeastOneNilChild.map { descendent ->
            tree.root.pathTo(descendent)
                .filter { (it as? RedBlackNode)?.color == NodeColor.BLACK }
        }

        assert(paths.isNotEmpty())
        assert(paths.all { path ->
            path.size == paths.first().size
        })
    }

    private val `15`: RedBlackNode<Int, String>  = RedBlackNode(15, "a")
    private val `11`: RedBlackNode<Int, String>  = RedBlackNode(11, "b")
    private val `19`: RedBlackNode<Int, String>  = RedBlackNode(19, "c")
    private val `7`: RedBlackNode<Int, String>  = RedBlackNode(7, "d")
    private val `13`: RedBlackNode<Int, String>  = RedBlackNode(13, "e")
    private val `23`: RedBlackNode<Int, String>  = RedBlackNode(23, "f")
    private val `18`: RedBlackNode<Int, String>  = RedBlackNode(18, "g")

    private fun <T: Comparable<T>, R> Node<T, R>.hasRelationships(
        parent: Node<T, R> = NilNode(),
        leftChild: Node<T, R> = NilNode(),
        rightChild: Node<T, R> = NilNode()
    ): Boolean = this.parent == parent && this.leftChild == leftChild && this.rightChild == rightChild

    private fun getTreeForTransplantTests(): RedBlackTree<Int, String> {
        return RedBlackTree<Int, String>().apply {
            listOf(`15`, `11`, `19`, `7`, `13`, `23`)
            .forEach(this::insert)
        };
    }

    private fun <T: Comparable<T>, R> getErrorString(actual: Node<T, R>, expectedParent: Node<T, R>? = null, expectedLeftChild: Node<T, R>? = null, expectedRightChild: Node<T, R>? = null): String {
        return "expected(parent = ${expectedParent?.toString() ?: "Nil"}, leftChild = ${expectedLeftChild?.toString() ?: "Nil"}, rightChild = ${expectedRightChild?.toString() ?: "Nil"}) != actual(parent = ${actual.parent}, leftChild = ${actual.leftChild}, rightChild = ${actual.rightChild})"
    }

    private fun assertInitialStateCorrectForTransplantTestsTree(tree: RedBlackTree<Int, String>) {
        assert(`15` == tree.root) { "Node 15 should be root" }
        assert(`15`.hasRelationships(NilNode(), `11`, `19`)) {
            "expected(parent is Nil, leftChild = 11, rightChild = 19) != actual(parent = ${`15`.parent}, leftChild = ${`15`.leftChild}, rightChild = ${`15`.rightChild})"
        }
        assert(`11`.hasRelationships(`15`, `7`, `13`)) {
            "expected(parent = 15, leftChild = 7, rightChild = 13) != actual(parent = ${`11`.parent}, leftChild = ${`11`.leftChild}, rightChild = ${`11`.rightChild})"
        }
        assert(`19`.hasRelationships(parent = `15`, rightChild =  `23`)) {
            "expected(parent = 15, leftChild is Nil, rightChild = 23) != actual(parent = ${`19`.parent}, leftChild = ${`19`.leftChild}, rightChild = ${`19`.rightChild})"
        }
        assert(`7`.hasRelationships(`11`)) {
            "expected(parent = 11, leftChild is Nil, rightChild is Nil) != actual(parent = ${`7`.parent}, leftChild = ${`7`.leftChild}, rightChild = ${`7`.rightChild})"
        }
        assert(`13`.hasRelationships(`11`)) {
            "expected(parent = 11, leftChild is Nil, rightChild is Nil) != actual(parent = ${`13`.parent}, leftChild = ${`13`.leftChild}, rightChild = ${`13`.rightChild})"
        }
        assert(`23`.hasRelationships(`19`)) {
            "expected(parent = 19, leftChild is Nil, rightChild is Nil) != actual(parent = ${`23`.parent}, leftChild = ${`23`.leftChild}, rightChild = ${`23`.rightChild})"
        }
    }
    @Test
    fun `transplant when u is root`() {
        val tree: RedBlackTree<Int, String> = getTreeForTransplantTests()

        assertInitialStateCorrectForTransplantTestsTree(tree)

        tree.transplant(`15`, `19`)


        assert(`19` === tree.root) { "Node 19 should be root" }
        assert(`15`.hasRelationships(leftChild = `11`, rightChild = `19`)) {
            "expected(parent is Nil, leftChild = 11, rightChild = 19) != actual(parent = ${`15`.parent}, leftChild = ${`15`.leftChild}, rightChild = ${`15`.rightChild})"
        }
        assert(`11`.hasRelationships(`15`, `7`, `13`)) {
            "expected(parent = 15, leftChild = 7, rightChild = 13) != actual(parent = ${`11`.parent}, leftChild = ${`11`.leftChild}, rightChild = ${`11`.rightChild})"
        }
        assert(`19`.hasRelationships(rightChild = `23`)) {
            "expected(parent is Nil, leftChild is Nil, rightChild = 23) != actual(parent = ${`19`.parent}, leftChild = ${`19`.leftChild}, rightChild = ${`19`.rightChild})"
        }
        assert(`7`.hasRelationships(`11`)) {
            "expected(parent = 11, leftChild is Nil, rightChild is Nil) != actual(parent = ${`7`.parent}, leftChild = ${`7`.leftChild}, rightChild = ${`7`.rightChild})"
        }
        assert(`13`.hasRelationships(`11`)) {
            "expected(parent = 11, leftChild is Nil, rightChild is Nil) != actual(parent = ${`13`.parent}, leftChild = ${`13`.leftChild}, rightChild = ${`13`.rightChild})"
        }
        assert(`23`.hasRelationships(`19`)) {
            "expected(parent = 19, leftChild is Nil, rightChild is Nil) != actual(parent = ${`23`.parent}, leftChild = ${`23`.leftChild}, rightChild = ${`23`.rightChild})"
        }
    }

    @Test
    fun `transplant when u is left child`() {
        val tree: RedBlackTree<Int, String> = getTreeForTransplantTests()
        assertInitialStateCorrectForTransplantTestsTree(tree)

        tree.transplant(`11`, `13`)

        assert(`15` == tree.root)
        assert(`15`.hasRelationships(NilNode(), `13`, `19`)) {
            getErrorString(`15`, null, `13`, `19`)
        }
        assert(`11`.hasRelationships(`15`, `7`, `13`)) {
            getErrorString(`11`, `15`, `7`, `13`)
        }
        assert(`19`.hasRelationships(`15`, NilNode(), `23`)) {
            getErrorString(`19`, `15`, null, `23`)
        }
        assert(`7`.hasRelationships(`11`)) {
            getErrorString(`7`, `11`)
        }
        assert(`13`.hasRelationships(`15`)) {
            getErrorString(`13`, `15`)
        }
        assert(`23`.hasRelationships(`19`)) {
            getErrorString(`23`, `19`)
        }
    }

    @Test
    fun `transplant when u is right child`() {
        val tree: RedBlackTree<Int, String> = getTreeForTransplantTests()
        assertInitialStateCorrectForTransplantTestsTree(tree)
        tree.insert(`18`)

        tree.transplant(`19`, `18`)
        assert(`15` == tree.root) { "Node 15 should be root"}

        assert(`15`.hasRelationships(leftChild = `11`, rightChild = `18`)) {
            getErrorString(`15`, expectedLeftChild = `11`, expectedRightChild = `18`)
        }

        assert(`11`.hasRelationships(parent = `15`, leftChild = `7`, rightChild = `13`)) {
            getErrorString(`11`, expectedParent = `15`, expectedLeftChild = `7`, expectedRightChild = `13`)
        }

        assert(`18`.hasRelationships(parent = `15`)) {
            getErrorString(`18`, expectedParent = `15`)
        }

        assert(`7`.hasRelationships(parent = `11`)) {
            getErrorString(`7`, expectedParent = `11`)
        }

        assert(`19`.hasRelationships(parent = `15`, leftChild = `18`, rightChild = `23`)) {
            getErrorString(`19`, expectedParent = `15`, expectedLeftChild = `18`, expectedRightChild = `23`)
        }

    }

    private fun getTreeForDeletionTests(): RedBlackTree<Int, String> {
        TODO()
    }

    private fun assertInitialStateCorrectForDeletionTestsTree() {
        TODO()
    }
    @Test
    fun `delete without fixup when left child is Nil`() {
        val tree = getTreeForDeletionTests()
        assertInitialStateCorrectForDeletionTestsTree()

        tree.delete(`19`)

        assert(`12`?.hasRelationships() ?: false)
    }






    // Interval Tests
    @Test
    fun `inserting into interval tree should update max endpoints`() {
        val tree = IntervalTree<Int, String>()

        val `17_19` = tree.ITNode(17, 19, "a")
        tree.insert(`17_19`)
        assertEquals(19, `17_19`.maxEndpoint, "(17, 19): ${`17_19`.maxEndpoint} should be 19")

        val `5_8` = tree.ITNode(5, 8, "a")
        tree.insert(`5_8`)
        assertEquals(19, `17_19`.maxEndpoint, "(17, 19): ${`17_19`.maxEndpoint} should be 19")
        assertEquals(8, `5_8`.maxEndpoint, "(5, 8): ${`5_8`.maxEndpoint} should be 8")

        val `21_24` = tree.ITNode(21, 24, "a")
        tree.insert(`21_24`)
        assertEquals(24, `17_19`.maxEndpoint, "(17, 19): ${`17_19`.maxEndpoint} should be 24")
        assertEquals(8, `5_8`.maxEndpoint, "(5, 8): ${`5_8`.maxEndpoint} should be 8")
        assertEquals(24, `21_24`.maxEndpoint, "(21, 24): ${`21_24`.maxEndpoint} should be 24")

        val `15_18` = tree.ITNode(15, 18, "a")
        tree.insert(`15_18`)
        assertEquals(24, `17_19`.maxEndpoint, "(17, 19): ${`17_19`.maxEndpoint} should be 24")
        assertEquals(18, `5_8`.maxEndpoint, "(5, 8): ${`5_8`.maxEndpoint} should be 18")
        assertEquals(24, `21_24`.maxEndpoint, "(21, 24): ${`21_24`.maxEndpoint} should be 24")
        assertEquals(18, `15_18`.maxEndpoint, "(21, 24): ${`15_18`.maxEndpoint} should be 18")
    }

    @Test
    fun `should be able to find first node intersecting`() {
        val tree = IntervalTree<Int, String>()

        val `17_19` = tree.ITNode(17, 19, "a")
        tree.insert(`17_19`)

        val `5_8` = tree.ITNode(5, 8, "b")
        tree.insert(`5_8`)

        val `21_24` = tree.ITNode(21, 24, "c")
        tree.insert(`21_24`)

        val `15_18` = tree.ITNode(15, 18, "d")
        tree.insert(`15_18`)

        assertEquals(`17_19`.value, tree.getIntervalContaining(17).value)
        assertEquals(`17_19`.value, tree.getIntervalContaining(18).value)

        assertEquals(`5_8`.value, tree.getIntervalContaining(5).value)
        assertEquals(`5_8`.value, tree.getIntervalContaining(6).value)
        assertEquals(`5_8`.value, tree.getIntervalContaining(7).value)

        assertEquals(`21_24`.value, tree.getIntervalContaining(21).value)
        assertEquals(`21_24`.value, tree.getIntervalContaining(22).value)
        assertEquals(`21_24`.value, tree.getIntervalContaining(23).value)

        assertEquals(`15_18`.value, tree.getIntervalContaining(15).value)
        assertEquals(`15_18`.value, tree.getIntervalContaining(16).value)

        assert(tree.getIntervalContaining(4) is NilNode)
        assert(tree.getIntervalContaining(8) is NilNode)
        assert(tree.getIntervalContaining(14) is NilNode)
        assert(tree.getIntervalContaining(19) is NilNode)
        assert(tree.getIntervalContaining(20) is NilNode)
        assert(tree.getIntervalContaining(24) is NilNode)
    }
}