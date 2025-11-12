package de.westnordost.streetcomplete.util.tree

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeTest {
    private val tree = buildTree {
        nd("1") {
            nd("1a")
            nd("1b") {
                nd("1b1")
            }
        }
        nd("2")
    }

    @Test
    fun `node builder`() {
        assertEquals(
            Node(null, listOf(
                Node("1", listOf(
                    Node("1a"),
                    Node("1b", listOf(
                        Node("1b1")
                    )),
                )),
                Node("2"),
            )),
            tree
        )
    }

    @Test
    fun yieldAll() {
        assertEquals(
            listOf("1", "1a", "1b", "1b1", "2"),
            tree.yieldAll().toList()
        )

        assertEquals(
            listOf(),
            buildTree<String> {  }.yieldAll().toList()
        )
    }

    @Test
    fun yieldBreadcrumbs() {
        assertEquals(
            null,
            tree.yieldBreadcrumbs("not found")
        )
        // top level
        assertEquals(
            listOf(),
            tree.yieldBreadcrumbs("2")?.toList()
        )
        // very nested
        assertEquals(
            listOf("1", "1b"),
            tree.yieldBreadcrumbs("1b1")?.toList()
        )
    }

    @Test
    fun find() {
        // root element
        assertEquals(
            tree,
            tree.findFirst(null)
        )
        // not found
        assertEquals(
            null,
            tree.findFirst("not found")
        )
        // on top level
        assertEquals(
            tree.children[0],
            tree.findFirst("1")
        )
        // nested
        assertEquals(
            tree.children[0].children[1].children[0],
            tree.findFirst("1b1")
        )
    }
}
