#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include <jgrapht_capi_types.h>
#include <jgrapht_capi.h>

char *expected="\
#Creator: JGraphT Lemon (LGF) Exporter\n\
#Version: 1\n\
\n\
@nodes\n\
label\n\
0\n\
1\n\
2\n\
\n\
@arcs\n\
		weight\n\
0	1	1.0\n\
1	2	1.0\n\n";

int main() {
    graal_isolate_t *isolate = NULL;
    graal_isolatethread_t *thread = NULL;

    if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
        fprintf(stderr, "graal_create_isolate error\n");
        exit(EXIT_FAILURE);
    }

    assert(jgrapht_capi_error_get_errno(thread) == 0);

    void *g;
    jgrapht_capi_graph_create(thread, 0, 0, 0, 0, &g);
    assert(jgrapht_capi_error_get_errno(thread) == 0);

    int v;
    int e;
    jgrapht_capi_graph_add_vertex(thread, g, NULL);
    jgrapht_capi_graph_add_vertex(thread, g, NULL);
    jgrapht_capi_graph_add_vertex(thread, g, NULL);

    jgrapht_capi_graph_add_edge(thread, g, 0, 1, NULL);
    jgrapht_capi_graph_add_edge(thread, g, 1, 2, NULL);
    jgrapht_capi_graph_add_edge(thread, g, 2, 3, NULL);
    jgrapht_capi_graph_add_edge(thread, g, 3, 0, NULL);

    // just test the API with a dummy file
    jgrapht_capi_export_file_lemon(thread, g, "dummy.lemon.out", 1, 0);

    // test output to string
    void *out;
    jgrapht_capi_export_string_lemon(thread, g, 1, 0, &out);
    char *str;
    jgrapht_capi_handles_get_ccharpointer(thread, out, &str);
    //printf("%s", str);
    assert(strcmp(str, expected) == 0);
    jgrapht_capi_handles_destroy(thread, out);

    jgrapht_capi_handles_destroy(thread, g);

    if (thread, graal_detach_thread(thread) != 0) {
        fprintf(stderr, "graal_detach_thread error\n");
        exit(EXIT_FAILURE);
    }

    return EXIT_SUCCESS;
}
