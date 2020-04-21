#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <jgrapht_capi.h>

void assert_coloring(graal_isolatethread_t *thread, void *g, void *c) { 
    void *map; 
    jgrapht_capi_coloring_get_vertex_color_map(thread, c, &map);
    void *eit = jgrapht_capi_graph_create_all_eit(thread, g);
    while(jgrapht_capi_it_hasnext(thread, eit)) { 
        long e = jgrapht_capi_it_next_long(thread, eit);
        long s = jgrapht_capi_graph_edge_source(thread, g, e);
        long t = jgrapht_capi_graph_edge_target(thread, g, e);
        long source_color, target_color;
        jgrapht_capi_map_long_long_get(thread, map, s, &source_color);
        jgrapht_capi_map_long_long_get(thread, map, t, &target_color);
        assert(source_color != target_color);
    }
    jgrapht_capi_destroy(thread, eit);
    jgrapht_capi_destroy(thread, map);
}

int main() {
    graal_isolate_t *isolate = NULL;
    graal_isolatethread_t *thread = NULL;

    if (thread, graal_create_isolate(NULL, &isolate, &thread) != 0) {
        fprintf(stderr, "graal_create_isolate error\n");
        exit(EXIT_FAILURE);
    }

    assert(jgrapht_capi_get_errno(thread) == 0);

    void *g = jgrapht_capi_graph_create(thread, 0, 0, 0, 0);
    assert(jgrapht_capi_get_errno(thread) == 0);

    assert(!jgrapht_capi_graph_is_directed(thread, g));
    assert(jgrapht_capi_graph_is_undirected(thread, g));
    assert(!jgrapht_capi_graph_is_weighted(thread, g));
    assert(!jgrapht_capi_graph_is_allowing_selfloops(thread, g));
    assert(!jgrapht_capi_graph_is_allowing_multipleedges(thread, g));

    assert(jgrapht_capi_graph_add_vertex(thread, g) == 0);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 1);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 2);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 3);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 4);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 5);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 6);
    assert(jgrapht_capi_graph_add_vertex(thread, g) == 7);

    assert(jgrapht_capi_graph_add_edge(thread, g, 0, 1) == 0);
    assert(jgrapht_capi_graph_add_edge(thread, g, 1, 2) == 1);
    assert(jgrapht_capi_graph_add_edge(thread, g, 2, 3) == 2);
    assert(jgrapht_capi_graph_add_edge(thread, g, 3, 0) == 3);
    assert(jgrapht_capi_graph_add_edge(thread, g, 4, 5) == 4);
    assert(jgrapht_capi_graph_add_edge(thread, g, 5, 6) == 5);
    assert(jgrapht_capi_graph_add_edge(thread, g, 6, 7) == 6);
    assert(jgrapht_capi_graph_add_edge(thread, g, 7, 4) == 7);
    assert(jgrapht_capi_graph_add_edge(thread, g, 3, 4) == 8);
    assert(jgrapht_capi_graph_add_edge(thread, g, 5, 3) == 9);

    void *c;
    jgrapht_capi_coloring_exec_greedy(thread, g, &c);
    assert(jgrapht_capi_get_errno(thread) == 0);
    long long colors;
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    jgrapht_capi_coloring_exec_greedy_smallestdegreelast(thread, g, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    jgrapht_capi_coloring_exec_backtracking_brown(thread, g, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    jgrapht_capi_coloring_exec_greedy_largestdegreefirst(thread, g, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    jgrapht_capi_coloring_exec_greedy_random_with_seed(thread, g, 13, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    jgrapht_capi_coloring_exec_greedy_dsatur(thread, g, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 3);
    assert_coloring(thread, g, c);
    jgrapht_capi_destroy(thread, c);

    // just test API, since this is not a coloring
    jgrapht_capi_coloring_exec_color_refinement(thread, g, &c);
    assert(jgrapht_capi_coloring_get_number_colors(thread, c, &colors) == 0);
    assert(colors == 8);
    jgrapht_capi_destroy(thread, c);

    // cleanup
    jgrapht_capi_destroy(thread, g);

    if (thread, graal_detach_thread(thread) != 0) {
        fprintf(stderr, "graal_detach_thread error\n");
        exit(EXIT_FAILURE);
    }

    return EXIT_SUCCESS;
}